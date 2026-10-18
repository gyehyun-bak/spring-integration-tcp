package com.example.client;

import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gradually adds callers (RAMP_PER_SEC new callers every second, up to MAX_CALLERS). Every caller invokes
 * {@link HostGateway} once per second, so the load per caller is fixed and only the number of callers grows.
 * Prints one CSV line per second and stops on the first failed call.
 */
@Component
@Profile("load-test")
@RequiredArgsConstructor
public class LoadTestRunner implements CommandLineRunner {

    private static final int MAX_CALLERS = 20_000;
    private static final int RAMP_PER_SEC = 100;
    private static final long INTERVAL_MS = 1000;
    private static final int HOLD_SEC = 10;

    private final HostGateway hostGateway;
    private final Queue<Long> latenciesMicros = new ConcurrentLinkedQueue<>();
    private final AtomicLong responses = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    private volatile boolean stopped = false;

    @Override
    public void run(String... args) throws Exception {
        var os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        var threads = ManagementFactory.getThreadMXBean();
        var runtime = Runtime.getRuntime();

        System.out.println("epochSec,elapsedSec,callers,errors,rps,p50Ms,p99Ms,maxMs,jvmThreads,jvmCpu,heapMb");
        int launched = 0;
        int heldSec = 0;
        long lastResponses = 0;
        long lastTick = System.nanoTime();
        for (int sec = 1; heldSec < HOLD_SEC; sec++) {
            for (int i = 0; i < RAMP_PER_SEC && launched < MAX_CALLERS; i++, launched++) {
                Thread.ofVirtual().start(this::call);
            }
            if (launched == MAX_CALLERS) {
                heldSec++;
            }
            Thread.sleep(1000);

            long now = System.nanoTime();
            long total = responses.get();
            List<Long> latencies = drain();
            System.out.printf("%d,%d,%d,%d,%.0f,%.1f,%.1f,%.1f,%d,%.2f,%.0f%n",
                    System.currentTimeMillis() / 1000, sec, launched, errors.get(),
                    (total - lastResponses) / ((now - lastTick) / 1e9),
                    percentileMs(latencies, 0.50), percentileMs(latencies, 0.99), percentileMs(latencies, 1.0),
                    threads.getThreadCount(), os.getProcessCpuLoad(),
                    (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024));
            lastResponses = total;
            lastTick = now;
            if (errors.get() > 0) {
                System.err.printf("Aborted: %d calls failed%n", errors.get());
                break;
            }
        }
        stopped = true;
        System.exit(0);
    }

    private void call() {
        try {
            long next = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ThreadLocalRandom.current().nextLong(INTERVAL_MS));
            while (!stopped) {
                long wait = next - System.nanoTime();
                if (wait > 0) {
                    TimeUnit.NANOSECONDS.sleep(wait);
                }
                next += TimeUnit.MILLISECONDS.toNanos(INTERVAL_MS);

                long start = System.nanoTime();
                if (hostGateway.sendAndReceive("hello") == null) {
                    throw new IllegalStateException("No reply");
                }
                latenciesMicros.add((System.nanoTime() - start) / 1000);
                responses.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            if (errors.incrementAndGet() == 1) {
                System.err.println("First failed call: " + e);
            }
        }
    }

    private List<Long> drain() {
        List<Long> drained = new ArrayList<>();
        for (Long latency; (latency = latenciesMicros.poll()) != null; ) {
            drained.add(latency);
        }
        Collections.sort(drained);
        return drained;
    }

    private static double percentileMs(List<Long> sortedMicros, double percentile) {
        if (sortedMicros.isEmpty()) {
            return 0;
        }
        int index = Math.min(sortedMicros.size() - 1, (int) (sortedMicros.size() * percentile));
        return sortedMicros.get(index) / 1000.0;
    }
}

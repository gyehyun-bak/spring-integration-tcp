package com.example.loadtest;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Gradually adds TCP connections to the echo host (--ramp-per-sec new connections every second, up to
 * --max-connections). Every connection sends one request per second, so the load per connection is fixed
 * and only the number of connections grows. Prints one CSV line per second.
 */
public class LoadTest {

    private static String host = "localhost";
    private static final int PORT = 9090;
    private static final Pattern VALUE = Pattern.compile("\"value\"\\s*:\\s*([-0-9.eE+]+)");
    private static final byte[] REQUEST = "0005hello".getBytes(StandardCharsets.US_ASCII);
    private static final long INTERVAL_MS = 1000;
    private static final int HOLD_SEC = 10;
    private static final long STALL_LIMIT_NANOS = TimeUnit.SECONDS.toNanos(10);

    private static final HttpClient http = HttpClient.newHttpClient();
    private static final AtomicLong connected = new AtomicLong();
    private static final AtomicLong connectFailures = new AtomicLong();
    private static final AtomicLong errors = new AtomicLong();
    private static final AtomicLong responses = new AtomicLong();
    private static final Recorder latencyMicros = new Recorder(TimeUnit.MINUTES.toMicros(1), 3);
    private static volatile boolean stopped = false;

    public static void main(String[] args) throws Exception {
        int maxConnections = Integer.parseInt(arg(args, "max-connections", "10000"));
        int rampPerSec = Integer.parseInt(arg(args, "ramp-per-sec", "100"));
        host = arg(args, "host", "localhost");

        System.out.println("epochSec,elapsedSec,connected,connectFailures,errors,rps,p50Ms,p99Ms,maxMs,hostThreads,hostCpu,hostHeapMb");
        int launched = 0;
        int heldSec = 0;
        long lastResponses = 0;
        Histogram interval = null;
        for (int sec = 1; heldSec < HOLD_SEC; sec++) {
            for (int i = 0; i < rampPerSec && launched < maxConnections; i++, launched++) {
                Thread.ofVirtual().start(LoadTest::run);
            }
            if (launched == maxConnections) {
                heldSec++;
            }
            long tickStart = System.nanoTime();
            Thread.sleep(1000);

            interval = latencyMicros.getIntervalHistogram(interval);
            long total = responses.get();
            System.out.printf("%d,%d,%d,%d,%d,%d,%.1f,%.1f,%.1f,%.0f,%.2f,%.0f%n",
                    System.currentTimeMillis() / 1000, sec, connected.get(), connectFailures.get(), errors.get(), total - lastResponses,
                    interval.getValueAtPercentile(50) / 1000.0,
                    interval.getValueAtPercentile(99) / 1000.0,
                    interval.getMaxValue() / 1000.0,
                    metric("jvm.threads.live"),
                    metric("process.cpu.usage"),
                    metric("jvm.memory.used?tag=area:heap") / (1024 * 1024));
            lastResponses = total;
            if (errors.get() > 0 || connectFailures.get() > 0) {
                System.err.printf("Aborted: connection dropped (errors=%d, connectFailures=%d)%n", errors.get(), connectFailures.get());
                break;
            }
            if (System.nanoTime() - tickStart > STALL_LIMIT_NANOS) {
                System.err.println("Aborted: a one-second tick took over 10s, the host is unresponsive");
                break;
            }
        }
        stopped = true;
        System.exit(0);
    }

    private static void run() {
        try (Socket socket = connect()) {
            if (socket == null) {
                return;
            }
            InputStream in = socket.getInputStream();
            var out = socket.getOutputStream();

            long next = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ThreadLocalRandom.current().nextLong(INTERVAL_MS));
            while (!stopped) {
                long wait = next - System.nanoTime();
                if (wait > 0) {
                    TimeUnit.NANOSECONDS.sleep(wait);
                }
                next += TimeUnit.MILLISECONDS.toNanos(INTERVAL_MS);

                long start = System.nanoTime();
                out.write(REQUEST);
                readResponse(in);
                responses.incrementAndGet();
                latencyMicros.recordValue(Math.min((System.nanoTime() - start) / 1000, TimeUnit.MINUTES.toMicros(1)));
            }
        } catch (IOException e) {
            errors.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Socket connect() {
        try {
            var socket = new Socket();
            socket.connect(new InetSocketAddress(host, PORT), 10_000);
            socket.setTcpNoDelay(true);
            connected.incrementAndGet();
            return socket;
        } catch (IOException e) {
            if (connectFailures.incrementAndGet() == 1) {
                System.err.println("First connect failure: " + e);
            }
            return null;
        }
    }

    private static void readResponse(InputStream in) throws IOException {
        byte[] header = in.readNBytes(4);
        if (header.length < 4) {
            throw new IOException("Connection closed by server");
        }
        int length = Integer.parseInt(new String(header, StandardCharsets.US_ASCII));
        if (in.readNBytes(length).length < length) {
            throw new IOException("Connection closed by server");
        }
    }

    private static double metric(String name) {
        try {
            var request = HttpRequest.newBuilder(URI.create("http://" + host + ":8080/actuator/metrics/" + name)).timeout(Duration.ofSeconds(3)).GET().build();
            String body = http.send(request, HttpResponse.BodyHandlers.ofString()).body();
            var matcher = VALUE.matcher(body);
            return matcher.find() ? Double.parseDouble(matcher.group(1)) : Double.NaN;
        } catch (IOException | InterruptedException e) {
            return Double.NaN;
        }
    }

    private static String arg(String[] args, String name, String defaultValue) {
        String prefix = "--" + name + "=";
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return defaultValue;
    }
}

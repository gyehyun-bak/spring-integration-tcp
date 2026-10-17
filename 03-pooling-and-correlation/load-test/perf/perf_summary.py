"""Summarises `perf script -F tid,time,event,trace` output (stdin) for the threads of one process, as CSV rows:
one row for all threads and one per thread-name group.

  schDelay   = time from sched_waking (thread becomes runnable) to sched_switch (thread starts running)
  run        = time on CPU between being switched in and out
  voluntary  = switched out while blocked (prev_state != R), preempted = switched out while still runnable (R)
Usage: perf script ... | python3 perf_summary.py <host_pid> <window_seconds>
"""
import os
import re
import sys
import time
from collections import defaultdict

SWITCH = re.compile(r"prev_pid=(\d+) .*?prev_state=(\S+) ==> .*? next_pid=(\d+)")
WAKING = re.compile(r"pid=(\d+)")

pid, window = sys.argv[1], float(sys.argv[2])


def thread_names(pid):
    names = {}
    for tid in os.listdir(f"/proc/{pid}/task"):
        try:
            with open(f"/proc/{pid}/task/{tid}/comm") as f:
                names[int(tid)] = re.sub(r"\d+", "#", f.read().strip())
        except OSError:
            pass
    return names


names = thread_names(pid)
woken_at = {}
switched_in_at = {}
voluntary = defaultdict(int)
preempted = defaultdict(int)
delays = defaultdict(list)
runs = defaultdict(list)

for line in sys.stdin:
    if "sched_waking:" in line:
        m = WAKING.search(line)
        if m and int(m.group(1)) in names:
            woken_at[int(m.group(1))] = float(line.split(":", 1)[0].split()[-1])
    elif "sched_switch:" in line:
        t = float(line.split(":", 1)[0].split()[-1])
        m = SWITCH.search(line)
        if not m:
            continue
        prev, state, nxt = int(m.group(1)), m.group(2), int(m.group(3))
        if prev in names:
            for key in ("all", names[prev]):
                (preempted if state.startswith("R") else voluntary)[key] += 1
                if prev in switched_in_at:
                    runs[key].append(t - switched_in_at[prev])
            switched_in_at.pop(prev, None)
        if nxt in names:
            switched_in_at[nxt] = t
            if nxt in woken_at:
                d = t - woken_at.pop(nxt)
                for key in ("all", names[nxt]):
                    delays[key].append(d)

now = int(time.time())
for key in sorted(voluntary.keys() | preempted.keys(), key=lambda k: -(voluntary[k] + preempted[k])):
    d = sorted(delays[key]) or [0.0]
    r = runs[key] or [0.0]
    total = voluntary[key] + preempted[key]
    print(f"{now},{key.replace(',', ' ')},{total / window:.0f},{voluntary[key] / window:.0f},{preempted[key] / window:.0f},"
          f"{sum(d) / len(d) * 1000:.4f},{d[min(len(d) - 1, int(len(d) * 0.99))] * 1000:.4f},{d[-1] * 1000:.4f},"
          f"{sum(r) / len(r) * 1000:.4f}")

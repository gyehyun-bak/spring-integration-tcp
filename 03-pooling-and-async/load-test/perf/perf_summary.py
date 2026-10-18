"""Summarises `perf script -F comm,pid,tid,time,event,trace` output (stdin) for the threads of one process, as CSV rows:
one row for all threads and one per thread-name group.

  schDelay   = time from sched_waking (thread becomes runnable) to sched_switch (thread starts running)
  run        = time on CPU between being switched in and out
  voluntary  = switched out while blocked (prev_state != R), preempted = switched out while still runnable (R)
The thread ids printed inside the sched_* tracepoints can differ from the pid/tid in the sample header (they may
come from a different pid namespace), so the owning process of a traced thread is taken from the header of the
sched_switch event that switches that very thread out. Threads that never get switched out in the window are ignored.
Usage: perf script ... | python3 perf_summary.py <host_pid> <window_seconds>
"""
import re
import sys
import time
from collections import defaultdict

LINE = re.compile(r"^\s*(.+?)\s+(\d+)/(\d+)\s+([\d.]+):\s+(\S+):\s+(.*)$")
SWITCH = re.compile(r"prev_pid=(\d+) .*?prev_state=(\S+) ==> .*? next_pid=(\d+)")
WAKING = re.compile(r"pid=(\d+)")

target, window = int(sys.argv[1]), float(sys.argv[2])

events = []
owner = {}   # traced tid -> pid of the owning process (from the sample header)
name = {}    # traced tid -> thread name
for raw in sys.stdin:
    m = LINE.match(raw)
    if not m:
        continue
    t, event, rest = float(m.group(4)), m.group(5), m.group(6)
    events.append((t, event, rest))
    if event == "sched:sched_switch":
        s = SWITCH.search(rest)
        if s:
            traced_tid = int(s.group(1))
            owner[traced_tid] = int(m.group(2))
            name[traced_tid] = re.sub(r"\d+", "#", m.group(1).strip())


def mine(tid):
    return owner.get(tid) == target


woken_at = {}
switched_in_at = {}
voluntary = defaultdict(int)
preempted = defaultdict(int)
delays = defaultdict(list)
runs = defaultdict(list)

for t, event, rest in events:
    if event == "sched:sched_waking":
        m = WAKING.search(rest)
        if m and mine(int(m.group(1))):
            woken_at[int(m.group(1))] = t
    elif event == "sched:sched_switch":
        m = SWITCH.search(rest)
        if not m:
            continue
        prev, state, nxt = int(m.group(1)), m.group(2), int(m.group(3))
        if mine(prev):
            for key in ("all", name[prev]):
                (preempted if state.startswith("R") else voluntary)[key] += 1
                if prev in switched_in_at:
                    runs[key].append(t - switched_in_at[prev])
            switched_in_at.pop(prev, None)
        if mine(nxt):
            switched_in_at[nxt] = t
            if nxt in woken_at:
                d = t - woken_at.pop(nxt)
                for key in ("all", name[nxt]):
                    delays[key].append(d)

now = int(time.time())
for key in sorted(voluntary.keys() | preempted.keys(), key=lambda k: -(voluntary[k] + preempted[k])):
    d = sorted(delays[key]) or [0.0]
    r = runs[key] or [0.0]
    total = voluntary[key] + preempted[key]
    print(f"{now},{key.replace(',', ' ')},{total / window:.0f},{voluntary[key] / window:.0f},{preempted[key] / window:.0f},"
          f"{sum(d) / len(d) * 1000:.4f},{d[min(len(d) - 1, int(len(d) * 0.99))] * 1000:.4f},{d[-1] * 1000:.4f},"
          f"{sum(r) / len(r) * 1000:.4f}")

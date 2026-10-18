#!/bin/sh
# Records scheduler events for $2 seconds and appends a summary of process $1 (host PID) to $3.
# Runs inside the privileged perf container (--pid=host).
pid=$1
secs=$2
out=$3
perf sched record -o /tmp/perf.data -- sleep "$secs" >/dev/null 2>&1
perf script -i /tmp/perf.data -F comm,pid,tid,time,event,trace 2>/dev/null | python3 /perf/perf_summary.py "$pid" "$secs" >> "$out"
rm -f /tmp/perf.data

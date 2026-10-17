#!/bin/sh
# Runs inside the host container (JVM is PID 1). Prints: containerMemBytes,voluntaryCtxSwitches,involuntaryCtxSwitches
mem=$(cat /sys/fs/cgroup/memory.current 2>/dev/null || cat /sys/fs/cgroup/memory/memory.usage_in_bytes)
# context switches are kept per thread, so sum over all live threads
ctx=$(cat /proc/1/task/*/status 2>/dev/null | awk '/^voluntary_ctxt_switches/ {v += $2} /^nonvoluntary_ctxt_switches/ {n += $2} END {print v "," n}')
echo "$mem,$ctx"

# Runs the host in a memory-limited container and ramps connections from 0 to -MaxConnections from a second container.
# A privileged perf container records scheduler events (perf sched) of the host JVM in short windows during the ramp.
# Prereq (repo root): ./gradlew :03-pooling-and-correlation:host:bootJar :03-pooling-and-correlation:load-test:installDist
#                     docker build -t tcpbench-perf load-test/perf
# Usage: ./run.ps1 -Label net
param(
    [string]$Label = 'run',
    [int]$MaxConnections = 20000,
    [string]$Memory = '2g'
)

$image = 'eclipse-temurin:21-jre'
$hostJar = (Get-ChildItem "$PSScriptRoot/../host/build/libs/*.jar" | Where-Object { $_.Name -notlike '*plain*' } | Select-Object -First 1).FullName
$loadTestDir = (Resolve-Path "$PSScriptRoot/build/install/load-test").Path
$sampleScript = (Resolve-Path "$PSScriptRoot/sample.sh").Path
$perfDir = (Resolve-Path "$PSScriptRoot/perf").Path
$outDir = (New-Item -ItemType Directory -Force "$PSScriptRoot/build").FullName

docker network create tcpbench 2>$null | Out-Null
docker rm -f echo-host loadgen perf 2>$null | Out-Null
docker run -d --name echo-host --network tcpbench --memory=$Memory --memory-swap=$Memory `
    -v "${hostJar}:/app/host.jar:ro" -v "${sampleScript}:/sample.sh:ro" $image `
    java -XX:MaxRAMPercentage=75 -jar /app/host.jar | Out-Null
$hostPid = docker inspect echo-host --format '{{.State.Pid}}'   # PID as seen from the Docker VM, which perf (--pid=host) uses

docker run -d --name perf --privileged --pid=host -v "${perfDir}:/perf:ro" -v "${outDir}:/out" tcpbench-perf sleep infinity | Out-Null
docker exec perf sh -c 'mount -t tracefs nodev /sys/kernel/tracing 2>/dev/null; true'
"epochSec,group,switchesPerSec,voluntaryPerSec,preemptedPerSec,schDelayMeanMs,schDelayP99Ms,schDelayMaxMs,runMeanMs" | Set-Content "$outDir/perf-$Label.csv"

$sampler = Start-Job -ArgumentList "$outDir/docker-$Label.csv" -ScriptBlock {
    param($file)
    "epochSec,containerMemBytes,voluntaryCtx,involuntaryCtx" | Set-Content $file
    while ($true) {
        $sample = docker exec echo-host sh /sample.sh
        "$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds()),$sample" | Add-Content $file
    }
}
$perfSampler = Start-Job -ArgumentList $hostPid, "/out/perf-$Label.csv" -ScriptBlock {
    param($pidInVm, $file)
    while ($true) {
        Start-Sleep -Seconds 8
        docker exec perf sh /perf/perf-window.sh $pidInVm 2 $file
    }
}
try {
    while (-not (docker logs echo-host 2>&1 | Select-String 'Started HostApplication')) { Start-Sleep -Seconds 1 }
    docker run --rm --name loadgen --network tcpbench -v "${loadTestDir}:/lt:ro" $image `
        sh /lt/bin/load-test --host=echo-host --max-connections=$MaxConnections | Tee-Object -FilePath "$outDir/ramp-$Label.csv"
} finally {
    Stop-Job $sampler, $perfSampler; Remove-Job $sampler, $perfSampler
    docker inspect echo-host --format 'OOMKilled={{.State.OOMKilled}} Running={{.State.Running}}' | Set-Content "$outDir/docker-state-$Label.txt"
    docker logs echo-host 2>&1 | Select-String -Pattern 'OutOfMemoryError|unable to create' -List | Out-File "$outDir/host-errors-$Label.txt"
    docker rm -f echo-host loadgen perf | Out-Null
}

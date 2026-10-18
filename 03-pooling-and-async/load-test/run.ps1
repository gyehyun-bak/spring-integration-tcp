# Ramps load against a memory-limited container while sampling it, and writes the results to build/.
# A privileged perf container records scheduler events (perf sched) of the measured JVM in short windows.
#   -Scenario host   : the host is measured; a separate load-test container ramps connections up to -MaxConnections.
#   -Scenario client : the client is measured; its LoadTestRunner ramps callers against the host (shared network namespace).
# Prereq (repo root): ./gradlew :03-pooling-and-correlation:host:bootJar :03-pooling-and-correlation:client:bootJar
#                                :03-pooling-and-correlation:load-test:installDist
#                     docker build -t tcpbench-perf load-test/perf
# Usage: ./run.ps1 -Label net
#        ./run.ps1 -Scenario client -Label client-net -HostMemory 4g
param(
    [string]$Label = 'run',
    [ValidateSet('host', 'client')][string]$Scenario = 'host',
    [int]$MaxConnections = 20000,
    [string]$HostMemory = '2g',
    [string]$ClientMemory = '2g'
)

docker info *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker is not running' }

$image = 'eclipse-temurin:21-jre'
$jvmOptions = '-XX:MaxRAMPercentage=75'
$hostJar = (Get-ChildItem "$PSScriptRoot/../host/build/libs/*.jar" | Where-Object { $_.Name -notlike '*plain*' } | Select-Object -First 1).FullName
$clientJar = (Get-ChildItem "$PSScriptRoot/../client/build/libs/*.jar" | Where-Object { $_.Name -notlike '*plain*' } | Select-Object -First 1).FullName
$loadTestDir = (Resolve-Path "$PSScriptRoot/build/install/load-test").Path
$sampleScript = (Resolve-Path "$PSScriptRoot/sample.sh").Path
$perfDir = (Resolve-Path "$PSScriptRoot/perf").Path
$outDir = (New-Item -ItemType Directory -Force "$PSScriptRoot/build").FullName
$measured = if ($Scenario -eq 'host') { 'echo-host' } else { 'echo-client' }

docker network create tcpbench 2>$null | Out-Null
docker rm -f echo-host echo-client loadgen perf 2>$null | Out-Null
docker run -d --name echo-host --network tcpbench --memory=$HostMemory --memory-swap=$HostMemory `
    -v "${hostJar}:/app/host.jar:ro" -v "${sampleScript}:/sample.sh:ro" $image `
    java $jvmOptions -jar /app/host.jar | Out-Null
while (-not (docker logs echo-host 2>&1 | Select-String 'Started HostApplication')) { Start-Sleep -Seconds 1 }

if ($Scenario -eq 'client') {
    # sharing the host's network namespace lets the client keep connecting to localhost:9090
    docker run -d --name echo-client --network container:echo-host --memory=$ClientMemory --memory-swap=$ClientMemory `
        -v "${clientJar}:/app/client.jar:ro" -v "${sampleScript}:/sample.sh:ro" $image `
        java $jvmOptions -jar /app/client.jar --spring.profiles.active=load-test --spring.main.web-application-type=none `
        --spring.main.banner-mode=off --logging.level.root=WARN | Out-Null
}
$measuredPid = docker inspect $measured --format '{{.State.Pid}}'   # PID as seen from the Docker VM, which perf (--pid=host) uses

docker run -d --name perf --privileged --pid=host -v "${perfDir}:/perf:ro" -v "${outDir}:/out" tcpbench-perf sleep infinity | Out-Null
docker exec perf sh -c 'mount -t tracefs nodev /sys/kernel/tracing 2>/dev/null; true'
"epochSec,group,switchesPerSec,voluntaryPerSec,preemptedPerSec,schDelayMeanMs,schDelayP99Ms,schDelayMaxMs,runMeanMs" | Set-Content "$outDir/perf-$Label.csv"

$sampler = Start-Job -ArgumentList $measured, "$outDir/docker-$Label.csv" -ScriptBlock {
    param($container, $file)
    "epochSec,containerMemBytes,voluntaryCtx,involuntaryCtx" | Set-Content $file
    while ($true) {
        $sample = docker exec $container sh /sample.sh
        "$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds()),$sample" | Add-Content $file
    }
}
$perfSampler = Start-Job -ArgumentList $measuredPid, "/out/perf-$Label.csv", "$outDir/perf-errors-$Label.txt" -ScriptBlock {
    param($pidInVm, $file, $errorFile)
    while ($true) {
        Start-Sleep -Seconds 8
        docker exec perf sh /perf/perf-window.sh $pidInVm 2 $file 2>> $errorFile
    }
}
try {
    if ($Scenario -eq 'host') {
        docker run --rm --name loadgen --network tcpbench -v "${loadTestDir}:/lt:ro" $image `
            sh /lt/bin/load-test --host=echo-host --max-connections=$MaxConnections | Tee-Object -FilePath "$outDir/ramp-$Label.csv"
    } else {
        docker wait echo-client | Out-Null   # the runner exits when it finishes or on the first failed call
        $log = docker logs echo-client 2>&1
        $log | Where-Object { $_ -match '^(epochSec|\d{10},)' } | Set-Content "$outDir/ramp-$Label.csv"
        $log | Where-Object { $_ -notmatch '^(epochSec|\d{10},)' } | Set-Content "$outDir/client-log-$Label.txt"
    }
} finally {
    Stop-Job $sampler, $perfSampler; Remove-Job $sampler, $perfSampler
    docker inspect $measured --format 'OOMKilled={{.State.OOMKilled}} Running={{.State.Running}}' | Set-Content "$outDir/docker-state-$Label.txt"
    docker logs $measured 2>&1 | Select-String -Pattern 'OutOfMemoryError|unable to create' -List | Out-File "$outDir/errors-$Label.txt"
    docker rm -f echo-host echo-client loadgen perf 2>$null | Out-Null
}

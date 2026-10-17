FROM ubuntu:24.04
RUN apt-get update \
    && apt-get install -y --no-install-recommends linux-tools-generic linux-tools-common python3 \
    && rm -rf /var/lib/apt/lists/*
# the generic package ships perf under a kernel-version directory that does not match the WSL2 kernel
RUN ln -s "$(ls /usr/lib/linux-tools/*/perf | head -1)" /usr/local/bin/perf

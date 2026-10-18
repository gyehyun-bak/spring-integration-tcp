package com.example.client;

import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "host", defaultReplyTimeout = "10000")
public interface HostGateway {
    String sendAndReceive(String message);
}

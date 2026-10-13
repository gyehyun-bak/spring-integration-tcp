package com.example.client;

import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "host")
public interface HostGateway {
    String sendAndReceive(String message);
}

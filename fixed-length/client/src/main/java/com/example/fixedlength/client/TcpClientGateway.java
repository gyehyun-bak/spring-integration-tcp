package com.example.fixedlength.client;

import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "tcpClientFlow.input")
public interface TcpClientGateway {
    String send(String message);
}

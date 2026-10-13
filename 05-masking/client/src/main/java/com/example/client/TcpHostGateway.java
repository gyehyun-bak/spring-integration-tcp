package com.example.client;

import com.example.client.dto.request.EchoRequestMessage;
import com.example.client.dto.response.EchoResponseMessage;
import com.example.client.dto.request.RepeatRequestMessage;
import com.example.client.dto.response.RepeatResponseMessage;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "host.request")
public interface TcpHostGateway {

    EchoResponseMessage echo(EchoRequestMessage request);

    RepeatResponseMessage repeat(RepeatRequestMessage request);
}

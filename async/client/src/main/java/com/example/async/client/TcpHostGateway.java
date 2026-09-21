package com.example.async.client;

import com.example.async.client.dto.request.EchoRequestMessage;
import com.example.async.client.dto.response.EchoResponseMessage;
import com.example.async.client.dto.request.RepeatRequestMessage;
import com.example.async.client.dto.response.RepeatResponseMessage;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "host.request")
public interface TcpHostGateway {

    EchoResponseMessage echo(EchoRequestMessage request);

    RepeatResponseMessage repeat(RepeatRequestMessage request);
}

package com.example.masking.client;

import com.example.masking.client.dto.request.EchoRequestMessage;
import com.example.masking.client.dto.response.EchoResponseMessage;
import com.example.masking.client.dto.request.RepeatRequestMessage;
import com.example.masking.client.dto.response.RepeatResponseMessage;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "host.request")
public interface TcpHostGateway {

    EchoResponseMessage echo(EchoRequestMessage request);

    RepeatResponseMessage repeat(RepeatRequestMessage request);
}

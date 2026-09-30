package com.example.fixedlength.client;

import com.example.fixedlength.client.dto.request.EchoRequestMessage;
import com.example.fixedlength.client.dto.response.EchoResponseMessage;
import com.example.fixedlength.client.dto.request.RepeatRequestMessage;
import com.example.fixedlength.client.dto.response.RepeatResponseMessage;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "host")
public interface HostGateway {

    EchoResponseMessage echo(EchoRequestMessage request);

    RepeatResponseMessage repeat(RepeatRequestMessage request);
}

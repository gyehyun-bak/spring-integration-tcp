package com.example.fixedlength.client;

import com.example.fixedlength.client.dto.EchoRequestMessage;
import com.example.fixedlength.client.dto.EchoResponseMessage;
import com.example.fixedlength.client.dto.RepeatRequestMessage;
import com.example.fixedlength.client.dto.RepeatResponseMessage;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "host")
public interface TcpHostGateway {

    EchoResponseMessage echo(EchoRequestMessage request);

    RepeatResponseMessage repeat(RepeatRequestMessage request);
}

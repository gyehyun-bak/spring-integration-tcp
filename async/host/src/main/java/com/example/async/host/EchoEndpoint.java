package com.example.async.host;

import com.example.async.host.dto.request.EchoRequestMessage;
import com.example.async.host.dto.response.EchoResponseMessage;
import com.example.async.host.dto.ResponseCode;
import com.example.async.host.dto.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import java.time.LocalDateTime;

import static com.example.async.host.SpringIntegrationConfig.HOST_RESPONSE_CHANNEL;
import static java.time.format.DateTimeFormatter.ofPattern;

@MessageEndpoint
@Slf4j
public class EchoEndpoint {

    @ServiceActivator(inputChannel = "host.request.echo", outputChannel = HOST_RESPONSE_CHANNEL)
    public EchoResponseMessage echo(EchoRequestMessage request) {
        EchoResponseMessage response = new EchoResponseMessage();
        response.setMessageUuid(request.getMessageUuid());
        response.setSender(Sender.HOST);
        response.setSentAt(LocalDateTime.now().format(ofPattern("yyyyMMddHHmmssSSS")));

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            response.setCode(ResponseCode.MISSING_VALUE);
            return response;
        }

        response.setCode(ResponseCode.SUCCESS);
        response.setMessage(request.getMessage());
        return response;
    }
}

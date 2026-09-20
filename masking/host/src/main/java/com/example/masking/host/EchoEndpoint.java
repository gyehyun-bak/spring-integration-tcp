package com.example.masking.host;

import com.example.masking.host.dto.request.EchoRequestMessage;
import com.example.masking.host.dto.response.EchoResponseMessage;
import com.example.masking.host.dto.ResponseCode;
import com.example.masking.host.dto.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import static com.example.masking.host.SpringIntegrationConfig.HOST_RESPONSE_CHANNEL;

@MessageEndpoint
@Slf4j
public class EchoEndpoint {

    @ServiceActivator(inputChannel = "host.request.echo", outputChannel = HOST_RESPONSE_CHANNEL)
    public EchoResponseMessage echo(EchoRequestMessage request) {
        EchoResponseMessage response = new EchoResponseMessage();
        response.setMessageUuid(request.getMessageUuid());
        response.setSender(Sender.HOST);

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            response.setCode(ResponseCode.MISSING_VALUE);
            return response;
        }

        response.setCode(ResponseCode.SUCCESS);
        response.setMessage(request.getMessage());
        return response;
    }
}

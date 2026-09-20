package com.example.fixedlength.host;

import com.example.fixedlength.host.dto.EchoRequestMessage;
import com.example.fixedlength.host.dto.EchoResponseMessage;
import com.example.fixedlength.host.dto.ResponseCode;
import com.example.fixedlength.host.dto.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import static com.example.fixedlength.host.SpringIntegrationConfig.RESPONSE_CHANNEL;

@MessageEndpoint
@Slf4j
public class EchoEndpoint {

    @ServiceActivator(inputChannel = "host.request.echo", outputChannel = RESPONSE_CHANNEL)
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

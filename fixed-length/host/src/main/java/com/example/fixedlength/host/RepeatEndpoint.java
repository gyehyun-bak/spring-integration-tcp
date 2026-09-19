package com.example.fixedlength.host;

import com.example.fixedlength.host.dto.RepeatRequestMessage;
import com.example.fixedlength.host.dto.RepeatResponseMessage;
import com.example.fixedlength.host.dto.ResponseCode;
import com.example.fixedlength.host.dto.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import java.util.Collections;

@MessageEndpoint
@Slf4j
public class RepeatEndpoint {

    private static final int MAX_REPEAT_COUNT = 100;

    @ServiceActivator(inputChannel = "REPEAT", outputChannel = SpringIntegrationConfig.HOST_RESPONSE_CHANNEL)
    public RepeatResponseMessage repeat(RepeatRequestMessage request) {
        log.atInfo()
                .addKeyValue("request", request)
                .log("Received REPEAT request.");

        RepeatResponseMessage response = new RepeatResponseMessage();
        response.setMessageUuid(request.getMessageUuid());
        response.setSender(Sender.HOST);
        response.setMessageCount(0);

        Integer repeatCount = request.getRepeatCount();
        if (repeatCount == null || request.getMessage() == null || request.getMessage().isBlank()) {
            response.setCode(ResponseCode.MISSING_VALUE);
            return response;
        }
        if (repeatCount < 1 || repeatCount > MAX_REPEAT_COUNT) {
            response.setCode(ResponseCode.INVALID_VALUE);
            return response;
        }

        response.setCode(ResponseCode.SUCCESS);
        response.setMessageCount(repeatCount);
        response.setMessages(Collections.nCopies(repeatCount, request.getMessage()));

        log.atInfo()
                .addKeyValue("response", response)
                .log("Sending REPEAT response.");

        return response;
    }
}

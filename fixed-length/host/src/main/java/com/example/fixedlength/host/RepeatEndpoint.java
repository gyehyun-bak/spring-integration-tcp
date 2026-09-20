package com.example.fixedlength.host;

import com.example.fixedlength.host.dto.request.RepeatRequestMessage;
import com.example.fixedlength.host.dto.response.RepeatResponseMessage;
import com.example.fixedlength.host.dto.ResponseCode;
import com.example.fixedlength.host.dto.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import java.util.Collections;

import static com.example.fixedlength.host.SpringIntegrationConfig.HOST_RESPONSE_CHANNEL;

@MessageEndpoint
@Slf4j
public class RepeatEndpoint {

    private static final int MAX_REPEAT_COUNT = 100;

    @ServiceActivator(inputChannel = "host.request.repeat", outputChannel = HOST_RESPONSE_CHANNEL)
    public RepeatResponseMessage repeat(RepeatRequestMessage request) {
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
        return response;
    }
}

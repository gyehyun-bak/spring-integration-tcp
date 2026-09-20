package com.example.fixedlength.client;

import com.example.fixedlength.client.dto.EchoRequestMessage;
import com.example.fixedlength.client.dto.EchoResponseMessage;
import com.example.fixedlength.client.dto.RepeatRequestMessage;
import com.example.fixedlength.client.dto.RepeatResponseMessage;
import com.example.fixedlength.client.dto.Sender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TcpTestController {

    private final TcpHostGateway tcpHostGateway;

    @PostMapping("/echo")
    public EchoResponseMessage echo(@RequestParam String message) {
        EchoRequestMessage request = new EchoRequestMessage();
        request.setMessageUuid(UUID.randomUUID().toString());
        request.setSender(Sender.CLIENT);
        request.setMessage(message);

        return tcpHostGateway.echo(request);
    }

    @PostMapping("/repeat")
    public RepeatResponseMessage repeat(@RequestParam int times, @RequestParam String message) {
        RepeatRequestMessage request = new RepeatRequestMessage();
        request.setMessageUuid(UUID.randomUUID().toString());
        request.setSender(Sender.CLIENT);
        request.setRepeatCount(times);
        request.setMessage(message);

        return tcpHostGateway.repeat(request);
    }
}

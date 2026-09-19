package com.example.echo.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TcpTestController {

    private final TcpClientGateway tcpClientGateway;

    @PostMapping("/echo")
    public String send(@RequestParam String message) {
        log.atInfo()
                .addKeyValue("message", message)
                .log("Sending message.");
        return tcpClientGateway.send(message);
    }
}

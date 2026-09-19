package com.example.simpleecho.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class TcpTestController {

    private final TcpClientGateway tcpClientGateway;

    @PostMapping("/send")
    public String send(@RequestParam String message) {
        log.atInfo()
                .addKeyValue("message", message)
                .log("Sent");
        return tcpClientGateway.send(message);
    }
}

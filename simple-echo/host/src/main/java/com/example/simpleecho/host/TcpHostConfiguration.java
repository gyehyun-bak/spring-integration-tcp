package com.example.simpleecho.host;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;

@Configuration
@Slf4j
public class TcpHostConfiguration {

    private static final int PORT = 9090;

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        return new TcpNetServerConnectionFactory(PORT);
    }

    @Bean
    public IntegrationFlow tcpServerFlow() {
        return IntegrationFlow.from(Tcp.inboundGateway(serverConnectionFactory()))
                .transform(Transformers.objectToString())
                .handle(((payload, headers) -> {
                    log.atInfo()
                            .addKeyValue("headers", headers)
                            .addKeyValue("payload", payload)
                            .log("Received.");
                    return "ECHO: " + payload;
                }))
                .get();
    }
}

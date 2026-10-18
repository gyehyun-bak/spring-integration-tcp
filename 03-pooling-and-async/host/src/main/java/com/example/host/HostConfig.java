package com.example.host;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;

import java.util.Map;

@Configuration
@Slf4j
public class HostConfig {

    private static final int PORT = 9090;

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        var factory = new TcpNioServerConnectionFactory(PORT);
        var serializer = new AsciiLengthHeaderSerializer();
        factory.setSerializer(serializer);
        factory.setDeserializer(serializer);
        factory.setBacklog(1024);
        return factory;
    }

    @Bean
    public IntegrationFlow tcpServerFlow() {
        return IntegrationFlow.from(Tcp.inboundGateway(serverConnectionFactory()))
                .transform(Transformers.fromJson(Map.class))
                .handle(Map.class, (request, headers) -> {
                    log.atDebug()
                            .addKeyValue("payload", request)
                            .addKeyValue("headers", headers)
                            .log("Message received.");
                    return Map.of("correlationId", request.get("correlationId"),
                            "message", "ECHO: " + request.get("message"));
                })
                .transform(Transformers.toJson())
                .get();
    }
}

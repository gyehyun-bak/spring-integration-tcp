package com.example.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
public class ClientConfig {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private static final long REPLY_TIMEOUT_MS = 10_000;

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        var factory = new TcpNioClientConnectionFactory(HOST, PORT);
        var serializer = new AsciiLengthHeaderSerializer();
        factory.setSerializer(serializer);
        factory.setDeserializer(serializer);
        return factory;
    }

    @Bean
    public IntegrationFlow requestFlow() {
        return IntegrationFlow.from("host")
                .enrichHeaders(h -> h.correlationIdFunction(message -> UUID.randomUUID().toString()))
                .wireTap(send -> send
                        .handle((message, headers) -> Map.of(
                                "correlationId", headers.get(IntegrationMessageHeaderAccessor.CORRELATION_ID),
                                "message", message))
                        .transform(Transformers.toJson())
                        .handle(Tcp.outboundAdapter(clientConnectionFactory())))
                .barrier(REPLY_TIMEOUT_MS, barrier -> barrier.id("replyBarrier"))
                .handle(List.class, (payloads, headers) -> payloads.get(1))   // List [request, reply]
                .get();
    }

    @Bean
    public IntegrationFlow replyFlow() {
        return IntegrationFlow.from(Tcp.inboundAdapter(clientConnectionFactory()))
                .transform(Transformers.fromJson(Map.class))
                .enrichHeaders(h -> h.<Map<String, String>>correlationIdFunction(m -> m.getPayload().get("correlationId")))
                .handle(Map.class, (reply, headers) -> reply.get("message"))
                .trigger("replyBarrier.handler")
                .get();
    }
}

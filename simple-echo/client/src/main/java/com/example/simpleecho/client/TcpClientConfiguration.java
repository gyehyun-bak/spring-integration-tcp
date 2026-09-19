package com.example.simpleecho.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.outbound.TcpOutboundGateway;

@Configuration
public class TcpClientConfiguration {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        return new TcpNetClientConnectionFactory(HOST, PORT);
    }

    @Bean
    public IntegrationFlow tcpClientFlow() {
        return IntegrationFlow.from("tcpClientFlow.input")
                .handle(Tcp.outboundGateway(clientConnectionFactory()))
                .transform(Transformers.objectToString())
                .get();
    }
}

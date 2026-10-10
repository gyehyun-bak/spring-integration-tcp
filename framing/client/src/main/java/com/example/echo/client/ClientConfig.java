package com.example.echo.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;

@Configuration
public class ClientConfig {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        var factory = new TcpNetClientConnectionFactory(HOST, PORT);
        var serializer = new AsciiLengthHeaderSerializer();
        factory.setSerializer(serializer);
        factory.setDeserializer(serializer);
        return factory;
    }

    @Bean
    public IntegrationFlow tcpClientFlow() {
        return IntegrationFlow.from("host")
                .handle(Tcp.outboundGateway(clientConnectionFactory()))
                .transform(Transformers.objectToString())
                .get();
    }
}

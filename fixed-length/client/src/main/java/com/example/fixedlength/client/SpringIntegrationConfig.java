package com.example.fixedlength.client;

import com.example.fixedlength.client.dto.HostMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;

@Configuration
public class SpringIntegrationConfig {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    public static final String REQUEST_CHANNEL = "host.request";

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        TcpNetClientConnectionFactory connectionFactory = new TcpNetClientConnectionFactory(HOST, PORT);
        MessageLengthHeaderSerializer serializer = new MessageLengthHeaderSerializer();
        connectionFactory.setSerializer(serializer);
        connectionFactory.setDeserializer(serializer);
        return connectionFactory;
    }

    @Bean
    public IntegrationFlow tcpClientFlow(HostMessageCodec codec) {
        return IntegrationFlow.from(REQUEST_CHANNEL)
                .transform(HostMessage.class, codec::encode)
                .handle(Tcp.outboundGateway(clientConnectionFactory()))
                .transform(byte[].class, codec::decode)
                .get();
    }
}

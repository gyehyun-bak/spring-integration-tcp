package com.example.fixedlength.host;

import com.example.fixedlength.host.dto.HostMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;

@Configuration
public class SpringIntegrationConfig {

    public static final String HOST_RESPONSE_CHANNEL = "hostResponse";
    private static final int PORT = 9090;

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        TcpNetServerConnectionFactory connectionFactory = new TcpNetServerConnectionFactory(PORT);
        MessageLengthHeaderSerializer serializer = new MessageLengthHeaderSerializer();
        connectionFactory.setSerializer(serializer);
        connectionFactory.setDeserializer(serializer);
        return connectionFactory;
    }

    @Bean
    public IntegrationFlow tcpServerFlow(HostMessageCodec codec) {
        return IntegrationFlow.from(Tcp.inboundGateway(serverConnectionFactory()))
                .transform(byte[].class, codec::decode)
                .route(HostMessage.class, request -> request.getTrxCode().name())
                .get();
    }

    @Bean
    public IntegrationFlow hostResponseFlow(HostMessageCodec codec) {
        return IntegrationFlow.from(HOST_RESPONSE_CHANNEL)
                .transform(HostMessage.class, codec::encode)
                .get();
    }
}

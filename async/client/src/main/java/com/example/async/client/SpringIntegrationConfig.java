package com.example.async.client;

import com.example.async.client.dto.HostMessage;
import com.example.async.client.mask.MaskedLoggingWireTap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;

@Configuration
@Slf4j
public class SpringIntegrationConfig {

    public static final String HOST_REQUEST_CHANNEL = "host.request";
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        var connectionFactory = new TcpNetClientConnectionFactory(HOST, PORT);
        var serializer = new AsciiLengthHeaderSerializer();
        connectionFactory.setSerializer(serializer);
        connectionFactory.setDeserializer(serializer);
        return connectionFactory;
    }

    @Bean
    public IntegrationFlow hostRequestFlow(HostMessageCodec codec, MaskedLoggingWireTap loggingWireTap) {
        return IntegrationFlow.from(HOST_REQUEST_CHANNEL)
                .wireTap(flow -> flow.handle(message -> loggingWireTap.log("Sending message.", message)))
                .transform(HostMessage.class, codec::encode)
                .handle(Tcp.outboundGateway(clientConnectionFactory()))
                .transform(byte[].class, codec::decode)
                .wireTap(flow -> flow.handle(message -> loggingWireTap.log("Received message.", message)))
                .get();
    }
}

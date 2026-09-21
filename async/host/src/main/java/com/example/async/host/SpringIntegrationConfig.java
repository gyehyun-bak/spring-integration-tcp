package com.example.async.host;

import com.example.async.host.dto.HostMessage;
import com.example.async.host.mask.MaskedLoggingWireTap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;

import java.util.Locale;

@Configuration
public class SpringIntegrationConfig {

    private static final int PORT = 9090;

    public static final String HOST_REQUEST_CHANNEL = "host.request";
    public static final String HOST_RESPONSE_CHANNEL = "host.response";

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        var connectionFactory = new TcpNetServerConnectionFactory(PORT);
        var serializer = new AsciiLengthHeaderSerializer();
        connectionFactory.setSerializer(serializer);
        connectionFactory.setDeserializer(serializer);
        return connectionFactory;
    }

    @Bean
    public IntegrationFlow hostRequestFlow(HostMessageCodec codec, MaskedLoggingWireTap loggingWireTap) {
        return IntegrationFlow.from(Tcp.inboundGateway(serverConnectionFactory()))
                .transform(byte[].class, codec::decode)
                .wireTap(flow -> flow.handle(message -> loggingWireTap.log("Received message.", message)))
                .route(HostMessage.class, SpringIntegrationConfig::getChannelByTrxCode)
                .get();
    }

    @Bean
    public IntegrationFlow hostResponseFlow(HostMessageCodec codec, MaskedLoggingWireTap loggingWireTap) {
        return IntegrationFlow.from(HOST_RESPONSE_CHANNEL)
                .wireTap(flow -> flow.handle(message -> loggingWireTap.log("Sending message.", message)))
                .transform(HostMessage.class, codec::encode)
                .get();
    }

    private static String getChannelByTrxCode(HostMessage message) {
        return HOST_REQUEST_CHANNEL + "." + message.getTrxCode().name().toLowerCase(Locale.ROOT);
    }
}

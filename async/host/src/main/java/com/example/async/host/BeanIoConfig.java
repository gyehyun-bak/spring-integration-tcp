package com.example.async.host;

import com.example.async.host.dto.request.EchoRequestMessage;
import com.example.async.host.dto.response.EchoResponseMessage;
import com.example.async.host.dto.request.RepeatRequestMessage;
import com.example.async.host.dto.response.RepeatResponseMessage;
import org.beanio.StreamFactory;
import org.beanio.builder.StreamBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanIoConfig {

    public static final String HOST_REQUEST_STREAM = "host.request";
    public static final String HOST_RESPONSE_STREAM = "host.response";

    @Bean
    StreamFactory beanIoStreamFactory() {
        StreamFactory factory = StreamFactory.newInstance();

        factory.define(new StreamBuilder(HOST_REQUEST_STREAM)
                .format("fixedlength")
                .addTypeHandler(String.class, new EucKrTypeHandler())
                .addRecord(EchoRequestMessage.class)
                .addRecord(RepeatRequestMessage.class));

        factory.define(new StreamBuilder(HOST_RESPONSE_STREAM)
                .format("fixedlength")
                .addTypeHandler(String.class, new EucKrTypeHandler())
                .addRecord(EchoResponseMessage.class)
                .addRecord(RepeatResponseMessage.class));

        return factory;
    }
}

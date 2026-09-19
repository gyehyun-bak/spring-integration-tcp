package com.example.fixedlength.host;

import com.example.fixedlength.host.dto.EchoRequestMessage;
import com.example.fixedlength.host.dto.EchoResponseMessage;
import com.example.fixedlength.host.dto.RepeatRequestMessage;
import com.example.fixedlength.host.dto.RepeatResponseMessage;
import org.beanio.StreamFactory;
import org.beanio.builder.StreamBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanIoConfig {

    public static final String REQUEST_STREAM = "request";
    public static final String RESPONSE_STREAM = "response";

    @Bean
    StreamFactory beanIoStreamFactory() {
        StreamFactory factory = StreamFactory.newInstance();

        factory.define(new StreamBuilder(REQUEST_STREAM)
                .format("fixedlength")
                .addTypeHandler(String.class, new EucKrTypeHandler())
                .addRecord(EchoRequestMessage.class)
                .addRecord(RepeatRequestMessage.class));

        factory.define(new StreamBuilder(RESPONSE_STREAM)
                .format("fixedlength")
                .addTypeHandler(String.class, new EucKrTypeHandler())
                .addRecord(EchoResponseMessage.class)
                .addRecord(RepeatResponseMessage.class));

        return factory;
    }
}

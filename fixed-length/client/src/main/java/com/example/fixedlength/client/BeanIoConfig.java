package com.example.fixedlength.client;

import org.beanio.StreamFactory;
import org.beanio.builder.StreamBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanIoConfig {

    @Bean
    StreamFactory beanIoStreamFactory() {
        EucKrTypeHandler eucKrTypeHandler = new EucKrTypeHandler();


        StreamBuilder builder = new StreamBuilder("host")
                .format("fixedlength")
                .addTypeHandler(String.class, eucKrTypeHandler);

        StreamFactory factory = StreamFactory.newInstance();
        factory.define(builder);

        return factory;
    }
}

package com.example.fixedlength.client.dto;

import lombok.Getter;
import lombok.Setter;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

@Record("echoResponse")
@Getter
@Setter
public class EchoResponseMessage extends HostMessage {
    @Field(length = 100)
    private String message;
}

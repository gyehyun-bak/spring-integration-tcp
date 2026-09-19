package com.example.fixedlength.client.dto;

import lombok.Getter;
import lombok.Setter;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

@Record("echoRequest")
@Getter
@Setter
public class EchoRequestMessage extends HostMessage {
    @Field(length = 100)
    private String message;
}

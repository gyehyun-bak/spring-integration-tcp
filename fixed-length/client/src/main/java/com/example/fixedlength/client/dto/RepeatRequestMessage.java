package com.example.fixedlength.client.dto;

import lombok.Getter;
import lombok.Setter;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

@Record("repeatRequest")
@Getter
@Setter
public class RepeatRequestMessage extends HostMessage {
    @Field(length = 4)
    private Integer repeatCount;

    @Field(length = 100)
    private String message;
}

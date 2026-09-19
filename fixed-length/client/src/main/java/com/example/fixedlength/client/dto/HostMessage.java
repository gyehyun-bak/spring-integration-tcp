package com.example.fixedlength.client.dto;

import lombok.Getter;
import lombok.Setter;
import org.beanio.annotation.Field;

@Getter
@Setter
public class HostMessage {

    @Field(length = 4)
    private Integer messageLength;

    @Field(length = 16)
    private String messageUuid;

    @Field(length = 6)
    private String sender;

    @Field(length = 17)
    private String sentAt;

    @Field(length = 6)
    private String trxCode;

    @Field(length = 6)
    private String code;
}

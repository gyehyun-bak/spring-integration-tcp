package com.example.async.client.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.beanio.annotation.Field;

@Getter
@Setter
@ToString
public abstract class HostMessage {

    @Field(ordinal = 1, length = 36)
    private String messageUuid;

    @Field(ordinal = 2, length = 6)
    private Sender sender;

    @Field(ordinal = 3, length = 17)
    private String sentAt;

    @Field(ordinal = 5, length = 6, format = "toString")
    private ResponseCode code;
}

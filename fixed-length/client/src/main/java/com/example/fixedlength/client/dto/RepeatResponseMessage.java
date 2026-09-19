package com.example.fixedlength.client.dto;

import lombok.Getter;
import lombok.Setter;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;
import org.beanio.annotation.Segment;

import java.util.List;

@Record("repeatResponse")
@Getter
@Setter
public class RepeatResponseMessage extends HostMessage {
    @Field(length = 4)
    private Integer messageCount;

    @Field(length = 100, minOccurs = 0, maxOccurs = 100, occursRef = "messageCount")
    private List<String> messages;
}

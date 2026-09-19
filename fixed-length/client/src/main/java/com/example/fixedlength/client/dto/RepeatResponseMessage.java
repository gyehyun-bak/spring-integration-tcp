package com.example.fixedlength.client.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;
import org.beanio.builder.Align;

import java.util.ArrayList;
import java.util.List;

@Record
@Getter
@Setter
@ToString(callSuper = true)
public class RepeatResponseMessage extends HostMessage {

    @Field(ordinal = 4, length = 6, rid = true, literal = "REPEAT")
    private TrxCode trxCode = TrxCode.REPEAT;

    @Field(ordinal = 6, length = 4, padding = '0', align = Align.RIGHT)
    private Integer messageCount;

    @Field(ordinal = 7, length = 100, collection = ArrayList.class, minOccurs = 0, maxOccurs = 100, occursRef = "messageCount")
    private List<String> messages = new ArrayList<>();
}

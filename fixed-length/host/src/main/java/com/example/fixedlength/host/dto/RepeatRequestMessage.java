package com.example.fixedlength.host.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;
import org.beanio.builder.Align;

@Record
@Getter
@Setter
@ToString(callSuper = true)
public class RepeatRequestMessage extends HostMessage {

    @Field(ordinal = 4, length = 6, rid = true, literal = "REPEAT")
    private TrxCode trxCode = TrxCode.REPEAT;

    @Field(ordinal = 6, length = 4, padding = '0', align = Align.RIGHT)
    private Integer repeatCount;

    @Field(ordinal = 7, length = 100)
    private String message;
}

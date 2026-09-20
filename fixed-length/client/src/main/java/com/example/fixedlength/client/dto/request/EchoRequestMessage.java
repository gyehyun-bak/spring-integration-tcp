package com.example.fixedlength.client.dto.request;

import com.example.fixedlength.client.dto.HostMessage;
import com.example.fixedlength.client.dto.TrxCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

@Record
@Getter
@Setter
@ToString(callSuper = true)
public class EchoRequestMessage extends HostMessage {

    @Field(ordinal = 4, length = 6, rid = true, literal = "ECHO")
    private TrxCode trxCode = TrxCode.ECHO;

    @Field(ordinal = 6, length = 100)
    private String message;
}

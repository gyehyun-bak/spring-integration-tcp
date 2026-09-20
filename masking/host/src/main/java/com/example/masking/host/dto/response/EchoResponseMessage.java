package com.example.masking.host.dto.response;

import com.example.masking.host.dto.HostMessage;
import com.example.masking.host.dto.TrxCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

@Record
@Getter
@Setter
@ToString(callSuper = true)
public class EchoResponseMessage extends HostMessage {

    @Field(ordinal = 4, length = 6, rid = true, literal = "ECHO")
    private TrxCode trxCode = TrxCode.ECHO;

    @Field(ordinal = 6, length = 100)
    private String message;
}

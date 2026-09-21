package com.example.async.host.dto.response;

import com.example.async.host.dto.HostMessage;
import com.example.async.host.dto.TrxCode;
import com.example.async.host.mask.Mask;
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
    @Mask(start = 2, end = 6, maskChar = '*')
    private String message;
}

package com.example.masking.host.dto.request;

import com.example.masking.host.dto.HostMessage;
import com.example.masking.host.dto.TrxCode;
import com.example.masking.host.mask.Mask;
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
    @Mask(start = 2, end = 6, maskChar = '*')
    private String message;
}

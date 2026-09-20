package com.example.fixedlength.client;

import com.example.fixedlength.client.dto.HostMessage;
import lombok.RequiredArgsConstructor;
import org.beanio.StreamFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class HostMessageCodec {

    private static final Charset BYTE_PRESERVING_CHARSET = StandardCharsets.ISO_8859_1;
    private static final DateTimeFormatter SENT_AT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final StreamFactory streamFactory;

    public byte[] encode(HostMessage request) {
        request.setSentAt(LocalDateTime.now().format(SENT_AT));

        return streamFactory.createMarshaller(BeanIoConfig.HOST_REQUEST_STREAM)
                .marshal(request)
                .toString()
                .getBytes(BYTE_PRESERVING_CHARSET);
    }

    public HostMessage decode(byte[] response) {
        return (HostMessage) streamFactory.createUnmarshaller(BeanIoConfig.HOST_RESPONSE_STREAM)
                .unmarshal(new String(response, BYTE_PRESERVING_CHARSET));
    }
}

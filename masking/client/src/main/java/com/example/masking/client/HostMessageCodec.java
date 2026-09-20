package com.example.masking.client;

import com.example.masking.client.dto.HostMessage;
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

    private final StreamFactory streamFactory;

    public byte[] encode(HostMessage request) {
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

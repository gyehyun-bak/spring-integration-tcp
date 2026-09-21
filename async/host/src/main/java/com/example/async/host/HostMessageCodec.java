package com.example.async.host;

import com.example.async.host.dto.HostMessage;
import lombok.RequiredArgsConstructor;
import org.beanio.StreamFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class HostMessageCodec {

    private static final Charset BYTE_PRESERVING_CHARSET = StandardCharsets.ISO_8859_1;

    private final StreamFactory streamFactory;

    public HostMessage decode(byte[] request) {
        return (HostMessage) streamFactory.createUnmarshaller(BeanIoConfig.HOST_REQUEST_STREAM)
                .unmarshal(new String(request, BYTE_PRESERVING_CHARSET));
    }

    public byte[] encode(HostMessage response) {
        return streamFactory.createMarshaller(BeanIoConfig.HOST_RESPONSE_STREAM)
                .marshal(response)
                .toString()
                .getBytes(BYTE_PRESERVING_CHARSET);
    }
}

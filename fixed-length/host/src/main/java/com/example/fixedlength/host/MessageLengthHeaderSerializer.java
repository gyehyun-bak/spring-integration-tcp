package com.example.fixedlength.host;

import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MessageLengthHeaderSerializer extends ByteArrayLengthHeaderSerializer {

    private static final int MESSAGE_LENGTH_SIZE = HEADER_SIZE_INT;

    public MessageLengthHeaderSerializer() {
        super(MESSAGE_LENGTH_SIZE);
    }

    @Override
    protected int readHeader(InputStream inputStream) throws IOException {
        byte[] messageLength = new byte[MESSAGE_LENGTH_SIZE];
        if (read(inputStream, messageLength, true) < 0) {
            throw new SoftEndOfStreamException("Stream closed between payloads");
        }
        return Integer.parseInt(new String(messageLength, StandardCharsets.US_ASCII));
    }

    @Override
    protected void writeHeader(OutputStream outputStream, int length) throws IOException {
        outputStream.write("%04d".formatted(length).getBytes(StandardCharsets.US_ASCII));
    }
}

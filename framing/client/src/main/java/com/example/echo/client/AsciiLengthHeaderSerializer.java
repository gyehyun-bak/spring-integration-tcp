package com.example.echo.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
public class AsciiLengthHeaderSerializer extends ByteArrayLengthHeaderSerializer {

    private static final int LENGTH_FIELD_BYTES = 4;
    private static final Pattern FOUR_DIGIT_PATTERN = Pattern.compile("\\d{4}");

    public AsciiLengthHeaderSerializer() {
        super(LENGTH_FIELD_BYTES);
    }

    @Override
    protected int readHeader(InputStream inputStream) throws IOException {
        byte[] lengthField = new byte[LENGTH_FIELD_BYTES];

        if (read(inputStream, lengthField, true) < 0) {
            throw new SoftEndOfStreamException("Stream closed between payloads");
        }

        return parseLengthFromFourDigitAscii(lengthField);
    }

    private int parseLengthFromFourDigitAscii(byte[] lengthField) {
        String lengthStr = new String(lengthField, StandardCharsets.US_ASCII);
        if (!FOUR_DIGIT_PATTERN.matcher(lengthStr).matches()) {
            throw new IllegalArgumentException("Invalid length field: [" + lengthStr + "]");
        }

        log.debug("Raw length field ASCII value is {}", lengthStr);

        return Integer.parseInt(lengthStr);
    }

    @Override
    protected void writeHeader(OutputStream outputStream, int length) throws IOException {
        outputStream.write(String.format(Locale.ROOT, "%04d", length).getBytes(StandardCharsets.US_ASCII));
    }
}

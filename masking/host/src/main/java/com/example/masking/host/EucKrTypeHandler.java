package com.example.masking.host;

import org.beanio.types.TypeHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EucKrTypeHandler implements TypeHandler {
    private static final Charset EUC_KR = Charset.forName("x-windows-949");

    @Override
    public Object parse(String text) {
        if (text == null || text.isEmpty()) return null;
        return new String(text.getBytes(StandardCharsets.ISO_8859_1), EUC_KR);
    }

    @Override
    public String format(Object value) {
        if (value == null) return "";
        return new String(value.toString().getBytes(EUC_KR), StandardCharsets.ISO_8859_1);
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }
}

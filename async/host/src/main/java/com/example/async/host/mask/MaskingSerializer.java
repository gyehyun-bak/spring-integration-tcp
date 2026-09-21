package com.example.async.host.mask;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class MaskingSerializer extends ValueSerializer<Object> {
    public static final String MASK_ENABLED = "MASK_ENABLED";

    private final int start;
    private final int end;
    private final char maskChar;

    public MaskingSerializer() {
        this(0, -1, '*');
    }

    private MaskingSerializer(int start, int end, char maskChar) {
        this.start = start;
        this.end = end;
        this.maskChar = maskChar;
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        if (property == null) return null;
        Mask ann = property.getAnnotation(Mask.class);
        return ann != null ? new MaskingSerializer(ann.start(), ann.end(), ann.maskChar()) : this;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        Boolean maskOn = (Boolean) ctxt.getAttribute(MASK_ENABLED);
        if (!Boolean.TRUE.equals(maskOn)) {
            ctxt.findValueSerializer(value.getClass()).serialize(value, gen, ctxt);
            return;
        }

        gen.writeString(mask(value.toString()));
    }

    private String mask(String value) {
        int len = value.length();
        int from = Math.clamp(start, 0, len);
        int to = (end < 0) ? len : Math.min(end, len);
        if (from >= to) return value;

        char[] chars = value.toCharArray();
        for (int i = from; i < to; i++) {
            chars[i] = maskChar;
        }
        return new String(chars);
    }
}

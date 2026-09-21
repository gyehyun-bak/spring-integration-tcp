package com.example.async.host.mask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class MaskedLoggingWireTap {
    private static final ObjectWriter MASKED_WRITER =
            JsonMapper.builder().build()
                    .writer()
                    .withAttribute(MaskingSerializer.MASK_ENABLED, true);

    public void log(String logMessage, Message<?> message) {
        log.atInfo().addKeyValue("message", toMaskedJson(message.getPayload())).log(logMessage);
    }

    private String toMaskedJson(Object payload) {
        try {
            return MASKED_WRITER.writeValueAsString(payload);
        } catch (Exception e) {
            return "[unserializable:" + payload.getClass().getSimpleName() + "]";
        }
    }
}

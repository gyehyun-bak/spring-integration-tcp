package com.example.masking.host.mask;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = MaskingSerializer.class)
public @interface Mask {
    int start() default 0;

    int end() default -1;

    char maskChar() default '*';
}

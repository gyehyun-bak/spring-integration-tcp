package com.example.fixedlength.host.dto;

public enum ResponseCode {

    SUCCESS("S00000"),
    INVALID_FORMAT("E00001"),
    MISSING_VALUE("E00002"),
    INVALID_VALUE("E00003"),
    SYSTEM_ERROR("E99999");

    private final String code;

    ResponseCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}

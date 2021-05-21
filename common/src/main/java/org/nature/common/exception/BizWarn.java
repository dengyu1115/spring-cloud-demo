package org.nature.common.exception;

public class BizWarn extends RuntimeException {

    private String code;

    public BizWarn(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

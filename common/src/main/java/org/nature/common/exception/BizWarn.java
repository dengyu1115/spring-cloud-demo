package org.nature.common.exception;

import org.nature.common.enums.Err;

public class BizWarn extends RuntimeException {

    private String code;

    public BizWarn(String message) {
        super(message);
        this.code = Err.BIZ.getCode();
    }

    public BizWarn(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

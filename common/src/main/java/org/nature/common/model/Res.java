package org.nature.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nature.common.enums.Err;

@NoArgsConstructor
@Getter
@Setter
public class Res<T> {

    private String code;

    private String msg;

    private T data;

    private Res(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Res<T> ok() {
        return ok(null);
    }

    public static <T> Res<T> ok(T data) {
        return new Res<>(Err.NORMAL.getCode(), null, data);
    }

    public static <T> Res<T> ok(String msg, T data) {
        return new Res<>(Err.NORMAL.getCode(), msg, data);
    }

    public static <T> Res<T> err() {
        return err(Err.SYSTEM.getCode(), Err.SYSTEM.getMsg());
    }

    public static <T> Res<T> err(String msg) {
        return new Res<>(Err.BIZ.getCode(), msg, null);
    }

    public static <T> Res<T> err(String code, String msg) {
        return new Res<>(code, msg, null);
    }
}

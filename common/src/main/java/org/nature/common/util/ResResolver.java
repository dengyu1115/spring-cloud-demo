package org.nature.common.util;

import org.nature.common.enums.Err;
import org.nature.common.exception.BizWarn;
import org.nature.common.model.Res;

import java.util.function.Supplier;

public class ResResolver {

    public static <T> T doInvoke(Supplier<Res<T>> supplier) {
        Res<T> res = supplier.get();
        if (res == null || Err.SYSTEM.getCode().equals(res.getCode())) {
            throw new RuntimeException("调用异常");
        }
        if (Err.BIZ.getCode().equals(res.getCode())) {
            throw new BizWarn(res.getMsg());
        }
        return res.getData();
    }
}

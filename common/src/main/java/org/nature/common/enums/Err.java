package org.nature.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Err {

    NORMAL("200", null),
    BIZ("400", null),
    SYSTEM("500", "system error");

    final String code;
    final String msg;

}

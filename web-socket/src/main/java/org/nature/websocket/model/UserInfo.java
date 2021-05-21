package org.nature.websocket.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

@EqualsAndHashCode
@Getter
@Setter
public class UserInfo extends BaseModel {

    private String system;

    private String id;

}

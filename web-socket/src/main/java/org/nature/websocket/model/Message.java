package org.nature.websocket.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

@Getter
@Setter
public class Message extends BaseModel {

    private String type;

    private String content;

}

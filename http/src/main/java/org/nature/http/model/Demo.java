package org.nature.http.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

@Getter
@Setter
public class Demo extends BaseModel {

    private String id;

    private String name;

    private String desc;
}

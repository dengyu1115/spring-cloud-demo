package org.nature.security.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

@Getter
@Setter
public class User extends BaseModel {

    private String id;
    private String username;
    private String password;

}

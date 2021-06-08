package org.nature.security.controller;


import org.nature.common.model.Res;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("security")
@RestController
public class SecurityController {

    @RequestMapping("test")
    public Res<String> test(@RequestBody Map<String, Object> req) {
        return Res.ok(req.get("data").toString());
    }

}

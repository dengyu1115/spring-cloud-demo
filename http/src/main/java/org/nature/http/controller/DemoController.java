package org.nature.http.controller;

import org.nature.common.model.Res;
import org.nature.http.model.Demo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("demo")
@RestController
public class DemoController {

    @RequestMapping("test")
    public Res<Demo> test(@RequestBody Demo req) {
        return Res.ok(req);
    }
}

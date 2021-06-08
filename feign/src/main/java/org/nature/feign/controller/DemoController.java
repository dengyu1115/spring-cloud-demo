package org.nature.feign.controller;

import org.nature.common.model.Res;
import org.nature.feign.model.Demo;
import org.nature.feign.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("demo")
@RestController
public class DemoController {

    @Autowired
    private DemoService demoService;

    @RequestMapping("test")
    public Res<Demo> test(@RequestBody Demo req) {
        Res<Demo> res = demoService.test(req);
        return Res.ok(res.getData());
    }
}

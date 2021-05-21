package org.nature.redis.controller;


import org.nature.redis.sender.DemoSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("redis")
@RestController
public class RedisController {

    @Autowired
    private DemoSender demoSender;

    @RequestMapping("send")
    @ResponseBody
    public void send(@RequestBody String msg) {
        demoSender.send(msg);
    }

}

package org.nature.websocket.controller;

import org.nature.redis.sender.DemoSender;
import org.nature.websocket.model.PushInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("message")
@RestController
public class MessageController {

    private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private DemoSender demoSender;

    @Autowired
    private HttpServletRequest req;

    @PostMapping("send")
    public void send(@RequestBody PushInfo req) {
        LOG.info("push message start req:{}", req);
        demoSender.send(req);
        LOG.info("push message end req:{}", req);
    }

}

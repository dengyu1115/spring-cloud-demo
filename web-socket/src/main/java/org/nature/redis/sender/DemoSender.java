package org.nature.redis.sender;

import org.nature.redis.constants.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DemoSender {

    @Autowired
    private RedisTemplate<String, Object> template;

    public void send(Object message) {
        template.convertAndSend(RedisConstant.CHANNEL, message);
    }
}

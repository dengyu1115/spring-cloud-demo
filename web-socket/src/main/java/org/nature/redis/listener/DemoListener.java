package org.nature.redis.listener;


import com.alibaba.fastjson.JSON;
import org.nature.websocket.model.PushInfo;
import org.nature.websocket.server.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class DemoListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(DemoListener.class);

    @Autowired
    private SocketServer socketServer;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();
        try {
            String text = new String(body, StandardCharsets.UTF_8);
            PushInfo pushInfo = JSON.parseObject(text, PushInfo.class);
            socketServer.send(pushInfo);
            LOG.info("consume end message:{}", message);
        } catch (Exception e) {
            LOG.info("consume error" + message, e);
        }

    }
}

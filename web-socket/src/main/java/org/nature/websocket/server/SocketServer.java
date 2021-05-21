package org.nature.websocket.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nature.websocket.model.Message;
import org.nature.websocket.model.PushInfo;
import org.nature.websocket.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;

@ServerEndpoint("/nature/server")
@Component
public class SocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(SocketServer.class);

    @OnOpen
    public void onOpen(Session session) {
        LOG.info("connection established session id:" + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("connection closed session id:" + session.getId());
        SessionContext.removeSession(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Message msg;
        try {
            msg = JSON.parseObject(message, Message.class);
            if (msg == null) {
                throw new NullPointerException("message is null");
            }
        } catch (Exception e) {
            throw new RuntimeException("message form error session id:" + session.getId(), e);
        }
        String type = msg.getType();
        if ("user_info".equals(type)) {
            UserInfo userInfo = JSON.parseObject(msg.getContent(), UserInfo.class);
            SessionContext.addSession(userInfo, session);
            LOG.info("user info:" + userInfo);
        } else {
            LOG.info("message:" + msg);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JSONObject jo = JSON.parseObject(message);
            if(jo.getJSONObject("content").getIntValue("count")==10){
                throw new RuntimeException("exception ..............");
            }
            LOG.info("message end:" + msg);
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOG.error("error occurred session id:" + session.getId(), t);
    }

    public void send(PushInfo req) {
        UserInfo info = new UserInfo();
        BeanUtils.copyProperties(req, info);
        List<Session> sessions = SessionContext.getSessions(info);
        for (Session session : sessions) {
            try {
                String content = req.getContent();
                session.getAsyncRemote().sendText(content);
                LOG.info("message send:{}", content);
            } catch (Exception e) {
                LOG.error(String.format("send error session id:%s", session.getId()), e);
            }

        }
    }
}

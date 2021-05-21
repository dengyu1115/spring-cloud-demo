package org.nature;


import com.alibaba.fastjson.JSON;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.nature.websocket.model.Message;
import org.nature.websocket.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class SocketTest {

    private static final Logger LOG = LoggerFactory.getLogger(SocketTest.class);
    private static String[] lines = new String[]{
            "8082,system01,id001",
            "8082,system02,id002",
            "8082,system03,id003",
            "8082,system04,id004",
            "8082,system02,id005",
            "8082,system01,id006",
            "8082,system03,id007",
            "8082,system04,id008",
            "8082,system01,id009",
            "8082,system02,id010",
            "8082,system03,id011",
            "8082,system03,id001",
            "8082,system01,id001",
            "8082,system02,id003",
            "8082,system03,id002",
            "8082,system04,id014",
            "8082,system02,id015",
            "8082,system01,id016",
            "8082,system03,id017",
            "8082,system04,id018",
            "8082,system01,id019",
            "8082,system02,id020",
            "8082,system03,id011",
            "8082,system03,id015",
            "8082,system01,id006",
            "8082,system03,id007",
            "8082,system04,id008",
            "8082,system01,id009",
            "8082,system02,id010",
            "8082,system03,id011",
            "8082,system03,id001",
            "8082,system01,id001",
            "8082,system02,id003",
            "8082,system03,id002",
            "8082,system04,id014",
            "8082,system02,id015",
            "8082,system01,id016",
            "8082,system03,id017",
            "8082,system02,id005",
            "8082,system01,id006",
            "8082,system03,id007",
            "8082,system04,id008",
            "8082,system01,id009",
            "8082,system02,id010",
            "8082,system03,id011",
            "8082,system03,id001",
            "8082,system01,id001",
            "8082,system02,id003",
            "8082,system03,id002",
            "8082,system04,id014",
            "8082,system02,id015"
    };

    public static void main(String[] args) throws URISyntaxException {
        multiRun();
        //createConnection(8082,"system01", "id001");
    }

    private static void multiRun() {
        LOG.info("lines length:" + lines.length);
        Arrays.stream(lines).forEach(i -> {
            String[] s = i.split(",");
            try {
                createConnection(Integer.parseInt(s[0]), s[1], s[2]);
            } catch (Exception e) {
                LOG.error(i, e);
            }
        });
    }

    private static void createConnection(int port, String system, String id) throws URISyntaxException {
        URI uri = new URI("ws://localhost:" + port + "/web-socket/nature/server");
        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                LOG.info("system:{} id:{} connected...", system, id);
                UserInfo info = new UserInfo();
                info.setSystem(system);
                info.setId(id);
                Message message = new Message();
                message.setType("user_info");
                message.setContent(JSON.toJSONString(info));
                this.send(JSON.toJSONString(message));
            }

            @Override
            public void onMessage(String s) {
                LOG.info("system:{} id:{} receive message:{}", system, id, s);
            }

            @Override
            public void onClose(int code, String reason, boolean isRemote) {
                LOG.info("system:{} id:{} closed code:{} reason:{} is remote:{}", system, id, code, reason, isRemote);
            }

            @Override
            public void onError(Exception e) {
                LOG.error(String.format("system:%s id:%s connected...", system, id), e);
            }
        };
        client.connect();

    }
}

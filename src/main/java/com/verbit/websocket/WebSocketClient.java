package com.verbit.websocket;

import jakarta.websocket.*;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebSocketClient {
    private static CountDownLatch messageLatch;
    private static final String SENT_MESSAGE = "Hello World";
    private static final String WEBSOCKET_ENDPOINT_URL = "PUT_URL_HERE";
    private static final String BEARER_TOKEN_VALUE = "PUT_BEARER_TOKEN_VALUE_HERE";

    public static void main(String [] args){
        try {
            messageLatch = new CountDownLatch(1);

            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().configurator(new AuthorizationBearerConfigurator()).build();
            ClientManager client = ClientManager.createClient();
            // enable debug logs
            client.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);
            client.connectToServer(new Endpoint() {

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                            System.out.println("Received message: "+message);
                            messageLatch.countDown();
                        });
                        session.getBasicRemote().sendText(SENT_MESSAGE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, cec, new URI(WEBSOCKET_ENDPOINT_URL));
            messageLatch.await(100, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class AuthorizationBearerConfigurator extends ClientEndpointConfig.Configurator {
        private static final String HEADER_NAME = "Authorization";
        private static final String token = BEARER_TOKEN_VALUE;
        private static final String HEADER_VALUE = "Bearer " + token;

        @Override
        public void beforeRequest(Map<String, List<String>> headers) {
            headers.put(HEADER_NAME, Collections.singletonList(HEADER_VALUE));
        }
    }
}
package com.coopsnakeserver.app.sockets;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/echo")
public class EchoServer {
    @OnMessage
    public String onMessage(String message) throws IOException, EncodeException {
        return message;
    }

    @OnOpen
    public void onOpen() throws IOException {
        System.out.println("hello from session open");
    }

    @OnClose
    public void onClose() throws IOException {
        System.out.println("hello from session close");
    }

    @OnError
    public void onError() {
        System.err.println("oops");
    }
}

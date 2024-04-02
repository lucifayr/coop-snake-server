package com.coopsnakeserver.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.coopsnakeserver.app.sockets.EchoServer;
import com.coopsnakeserver.app.sockets.TestSocket;

/**
 * Hello world!
 *
 */
public class App {
    static int PORT = 8765;

    public static void main(String[] args) {
        runServer();

    }

    public static void runServer() {

        Server server = new Server("localhost", PORT, "/websockets", EchoServer.class);

        try {

            server.start();

            var reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Please press a key to stop the server.");
            reader.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            server.stop();

        }

    }
}

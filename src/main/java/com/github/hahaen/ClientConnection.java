package com.github.hahaen;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientConnection extends Thread {
    private Socket socket;
    private int clientId;
    private String clientName;
    private Server server;

    public ClientConnection(int clientId, Server server, Socket socket) {
        this.clientId = clientId;
        this.server = server;
        this.socket = socket;
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (isNotOnlineYet()) {
                    clientName = line;
                    server.registerClient(this);
                } else {
                    Message message = JSON.parseObject(line, Message.class);
                    server.sendMassage(this, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.clientOffline(this);
        }
    }

    private boolean isNotOnlineYet() {
        return clientName == null;
    }


    public void sendMassage(String message) throws IOException {
        Util.writeMessage(socket, message);
    }
}

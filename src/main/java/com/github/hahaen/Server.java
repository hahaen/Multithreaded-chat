package com.github.hahaen;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Server {
    private static AtomicInteger COUNTER = new AtomicInteger(0);
    private final ServerSocket server;
    private final Map<Integer, ClientConnection> clients = new ConcurrentHashMap<>();

    //Tcp连接的端口号，0~65535
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
    }

    public void start() throws IOException {
        while (true) {
            Socket socket = server.accept();
            new ClientConnection(COUNTER.incrementAndGet(), this, socket).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server(8080).start();
    }

    public void registerClient(ClientConnection clientConnection) {
        clients.put(clientConnection.getClientId(), clientConnection);
        this.clientOnline(clientConnection);
    }

    private String getAllClientInfo() {
        return clients.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue().getClientName()).collect(Collectors.joining(","));
    }

    private void clientOnline(ClientConnection clientWhoHasJustLoggedIn) {
        clients.values().forEach(client -> dispatchMessage(client, "系统", "所有人", clientWhoHasJustLoggedIn.getClientName() + "上线了！" + getAllClientInfo()));
    }

    private void dispatchMessage(ClientConnection client, String src, String target, String message) {
        try {
            client.sendMassage(src + "对" + target + "说：" + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMassage(ClientConnection src, Message message) {
        if (message.getId() == 0) {
            clients.values().forEach(client -> dispatchMessage(client, src.getClientName(), "所有人", message.getMessage()));
        } else {
            int targetUser = message.getId();
            ClientConnection target = clients.get(targetUser);
            if (target == null) {
                System.err.println("用户" + targetUser + "不存在");
            } else {
                dispatchMessage(target, src.getClientName(), "你", message.getMessage());
            }
        }
    }

    public void clientOffline(ClientConnection clientConnection) {
        clients.remove(clientConnection.getClientId());
        clients.values().forEach(client -> dispatchMessage(client, "系统", "所有人", clientConnection.getClientName() + "下线了！" + getAllClientInfo()));
    }

}

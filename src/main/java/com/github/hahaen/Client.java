package com.github.hahaen;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("请输入你的昵称");
        Scanner userInput = new Scanner(System.in);
        String name = userInput.nextLine();

        Socket socket = new Socket("127.0.0.1", 8080);

        Util.writeMessage(socket, name);

        System.out.println("连接成功！");

        new Thread(() -> readFromServer(socket)).start();

        while (true) {

            System.out.println("请输入你要发送的聊天内容");
            System.out.println("id:message,例如：1:hello,表示id为1的用户发送hello的消息");
            System.out.println("id为0表示像所有人发送消息。例如：0:hello,表示向所以在线用户发送hello的消息");
            System.out.println("---------------------");

            String line = userInput.nextLine();

            if (!line.contains(":")) {
                System.err.println("输入的格式不对！");
            } else {
                int colonIndex = line.indexOf(":");
                int id = Integer.parseInt(line.substring(0, colonIndex));
                String message = line.substring(colonIndex + 1);

                String json = JSON.toJSONString(new Message(id, message));
                Util.writeMessage(socket, json);
            }
        }
    }

    private static void readFromServer(Socket socket) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

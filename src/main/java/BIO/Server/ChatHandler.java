package BIO.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author wengyinbing
 * @data 2021/6/22 21:56
 **/
public class ChatHandler implements Runnable{
    private ChatServer server;
    private Socket socket;

    public ChatHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            server.addClient(socket);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = null;
            while((msg = reader.readLine())!=null){
                String sendmsg = "Client[" + socket.getPort() + "]:" + msg;
                //服务器打印这个消息
                System.out.println(sendmsg);
                //群发这个消息
                server.sendMessage(socket,msg);
                if(msg.equals("quit")){
                    System.out.println("Client[" + socket.getPort() + "]:Offline");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {//结束或是意外终止记得移除socket
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

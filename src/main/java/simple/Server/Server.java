package simple.Server;

import com.sun.source.tree.NewArrayTree;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author wengyinbing
 * @data 2021/6/22 21:24
 **/
public class Server {
    public static void main(String[] args) {
        int port = 8888;//监听的接口
        try(ServerSocket server = new ServerSocket(port)){//serversocket只用来监听接口，socket用来传输数据
            System.out.println("启动服务，监听端口："+ port);
            while(true) {//阻塞式监听接口，没有连接的话，就会一直停留在这
                Socket socket = server.accept();
                System.out.println("[Client " + socket.getPort() + "] Online");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String msg = null;
                while ((msg = reader.readLine()) != null) {
                    System.out.println("[Client "+socket.getPort()+"]: "+msg);
                    writer.write(msg+"\n");
                    writer.flush();
                    if(msg.equals("quit")){
                        System.out.println("Client[" + socket.getPort() + "]:Offline");
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

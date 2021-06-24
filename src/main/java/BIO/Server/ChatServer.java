package BIO.Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author wengyinbing
 * @data 2021/6/22 21:56
 **/
public class ChatServer {
    private int port = 8888;
    private Map<Integer, Writer> map = new ConcurrentHashMap<>() ;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    public void sendMessage(Socket socket,String msg) throws IOException {
        //给其他的客户端发送信息
        for(Integer port:map.keySet()){
            if(!port.equals(socket.getPort())){
                //System.out.println("test Client["+port+"]: "+msg);
                Writer writer = map.get(port);
                writer.write("Client["+socket.getPort()+"]: "+msg+"\n");
                writer.flush();
            }
        }
    }
    public void addClient(Socket socket) throws IOException {
        if(socket != null){
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            map.put(socket.getPort(),writer);
            System.out.println("Client["+socket.getPort()+"]:Online");
        }
    }
    public void removeClient(Socket socket) throws IOException {
        if(socket != null){
            if(map.containsKey(socket.getPort())){
                map.get(socket.getPort()).close();
                map.remove(socket.getPort());
                System.out.println("Client[" + socket.getPort() + "]Offline");
            }
        }
    }
    public void start(){
        try(ServerSocket server = new ServerSocket(port)){
            while(true){
                Socket socket = server.accept();
                executorService.execute(new ChatHandler(this,socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}

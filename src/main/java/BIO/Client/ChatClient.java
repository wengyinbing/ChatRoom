package BIO.Client;

import BIO.Server.ChatServer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.util.concurrent.BrokenBarrierException;

/**
 * @author wengyinbing
 * @data 2021/6/22 21:56
 **/
public class ChatClient {
    private int port = 8888;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Socket socket;
    private final static String host = "127.0.0.1";

    public ChatClient(int port) {
        this.port = port;
    }
    public ChatClient(){

    }

    //接收数据
    public String receivemsg() throws IOException {
        String msg = null;
        if(!socket.isOutputShutdown()){//发送数据并未关闭
            msg = reader.readLine();
        }
        return msg;
    }
    //发送数据
    public void sendMessage(String msg) throws IOException {
        if(!socket.isInputShutdown()){
            writer.write(msg+"\n");
            //System.out.println("sendmessage");
            writer.flush();
        }
    }

    public void start() throws IOException {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //建立线程用来处理用户输入
            new Thread(new UserInputHandler(this)).start();
            String msg = null;//?
            while((msg = receivemsg())!=null){
                System.out.println(msg);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            if(writer!=null){
                writer.close();
            }
        }

    }

    public static void main(String[] args) throws IOException {
        new ChatClient().start();
    }
}

package AIO.Client;

import NIO.Client.ChatClent;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author wengyinbing
 * @data 2021/6/24 19:49
 **/
public class ChatClient {
    private Charset charset = Charset.forName("UTF-8");
    private String host;
    private int port;

    private static final Integer BUFFER = 1024;
    private AsynchronousSocketChannel socketChannel;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send(String msg){
        if(msg.isEmpty()){
            return ;
        }
        ByteBuffer buffer = charset.encode(msg);
        //返回未来获取到的结果
        Future<Integer> write = socketChannel.write(buffer);
        try{
            write.get();
        } catch (InterruptedException e) {
            System.out.println("消息发送失败！");
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.out.println("消息发送失败！");
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            socketChannel = AsynchronousSocketChannel.open();

            Future<Void> future = socketChannel.connect(new InetSocketAddress(host,port));
            future.get();

            //进行用户输入线程
            new Thread(new UserInputHandler(this)).start();

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
            while(true) {
                //一直运行，让客户端保持运行状态
                //将客户端获取的数据保存在buffer中
                Future<Integer> read = socketChannel.read(buffer);
                if (read.get() < 0) {
                    System.out.println("服务端断开连接");
                    if(socketChannel != null){
                        socketChannel.close();
                    }
                    System.exit(-1);
                }
                else{
                    buffer.flip();//状态从客户端写入变成读取状态
                    String msg = String.valueOf(charset.decode(buffer));
                    System.out.println(msg);
                    buffer.clear();//代表可以重新进行客户端从服务端接收了
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new ChatClient("127.0.0.1",8888).start();
    }
}

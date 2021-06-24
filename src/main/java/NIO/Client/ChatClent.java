package NIO.Client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author wengyinbing
 * @data 2021/6/23 16:44
 **/
public class ChatClent {
    private Charset charset = Charset.forName("UTF-8");//编码方式，字符串转换时用的上
    private final static Integer BUFFERSIZE = 1024;
    //初始化读写的缓冲区
    private ByteBuffer read_buffer = ByteBuffer.allocate(BUFFERSIZE);
    private ByteBuffer write_buffer = ByteBuffer.allocate(BUFFERSIZE);

    //声明全局是为了？？？
    private SocketChannel client;
    private Selector selector;
    private Integer port = 8888;


    public void start(){
        try{
            client = SocketChannel.open();
            selector = Selector.open();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress("127.0.0.1",port));

            while(true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for(SelectionKey key : selectionKeys){
                    handle(key);
                }
                selectionKeys.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClosedSelectorException e){
            //当用户输入quit时，在send()方法中，selector会被关闭，而在上面的无限while循环中，可能会使用到已经关闭了的selector。
            //所以这里捕捉一下异常，做正常退出处理就行了。不会对服务器造成影响
        }
    }

    public void handle(SelectionKey key) throws IOException {
        //处理事件 客户端的连接状态 与 read状态
        SocketChannel client = (SocketChannel) key.channel();
        if(key.isConnectable()){
            if(client.finishConnect()){//已经连接了 ，将建立一个线程用于客户端用户进行输入
                System.out.println("成功连接");
                new Thread(new UserInputHandler(this)).start();
            }
            client.register(selector,SelectionKey.OP_READ);//监听这个客户端的输入状态
        }
        if(key.isReadable()){
            String msg = receive(client);
            System.out.println(msg);
            if (msg.equals("quit")) {
                //解除该事件的监听
                key.cancel();
                //更新Selector
                selector.wakeup();
            }
        }
    }

    public String receive(SocketChannel client) throws IOException {
        read_buffer.clear();
        while(client.read(read_buffer)>0);
        read_buffer.flip();
        //接收消息
        return String.valueOf(charset.decode(read_buffer));
    }

    public void send(String msg) throws IOException {
        if(!msg.isEmpty()){
            write_buffer.clear();
            write_buffer.put(charset.encode(msg));
            write_buffer.flip();
            while(write_buffer.hasRemaining()){//当缓冲区只要还一个元素的时候就会返回true
                client.write(write_buffer);
            }
            if(msg.equals("quit")){
                selector.close();
            }
        }
    }

    public static void main(String[] args) {
        new ChatClent().start();
    }
}

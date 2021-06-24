package NIO.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author wengyinbing
 * @data 2021/6/23 16:44
 **/
public class ChatServer {
    private Charset charset = Charset.forName("UTF-8");//编码方式，字符串转换时用的上
    private Integer port;
    private final static Integer BUFFERSIZE = 1024;
    //初始化读写的缓冲区
    private ByteBuffer read_buffer = ByteBuffer.allocate(BUFFERSIZE);
    private ByteBuffer write_buffer = ByteBuffer.allocate(BUFFERSIZE);


    public ChatServer(int port){
        this.port = port;
    }
    public void start(){
        try{
            //开启
            ServerSocketChannel server = ServerSocketChannel.open();
            Selector selector = Selector.open();
            //默认是阻塞的，nio是非阻塞的 同步非阻塞需要设置为false
            server.configureBlocking(false);
            //将serversocket进行bind
            server.socket().bind(new InetSocketAddress(port));
            //将server 注册到selector上，并开始监听accept事件
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口："+port);
            while(true){
                if(selector.select()>0){
                    //select()会返回出发事件的数目
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    for(SelectionKey key:selectionKeys){
                        handler(key,selector);
                    }
                    //处理完成之后清空，防止事件重复处理
                    selectionKeys.clear();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handler(SelectionKey key,Selector selector) throws IOException {
        //处理服务端接受的状态 accept read
        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel socket = server.accept();
            socket.configureBlocking(false);//改成非阻塞的
            socket.register(selector,SelectionKey.OP_READ);
            System.out.println("客户端[" + socket.socket().getPort()+"] 上线了！");
        }
        if(key.isReadable()){//收到来自客户端的信息的通知啦
            SocketChannel client  = (SocketChannel) key.channel();
            String msg = receive(client);
            System.out.println("客户端[" + client.socket().getPort() + "]:" + msg);
            //将消息发送给其他的客户端
            sendMessage(client,msg,selector);
            if(msg.equals("quit")){
                //解除事件的监听
                key.channel();
                //更新selector
                selector.wakeup();
                System.out.println("客户端["+client.socket().getPort()+"] 下线了！");
            }
        }
    }
    //接收消息
    public String receive(SocketChannel client) throws IOException {
        //用之前先清空一下，防止信息残留
        read_buffer.clear();
        //将通道的数据读入到缓冲区中 buffer中，因为没有明确的\n标志结束位，就要将channel中的字节全部读取玩
        while(client.read(read_buffer)>0);
        //结束，将读写转换？？
        read_buffer.flip();
        return  String.valueOf(charset.decode(read_buffer));
    }

    //群发消息
    public void sendMessage(SocketChannel client,String msg,Selector selector) throws IOException {
        msg = "客户端[" + client.socket().getPort()+"]:"+msg;
        for(SelectionKey key : selector.keys()){
            //不是服务端 不是客户端自己 是有效的
            if(!(key.channel() instanceof ServerSocketChannel) && !client.equals(key.channel()) && key.isValid()){
                SocketChannel otherClient = (SocketChannel)key.channel();
                //System.out.println(otherClient.socket().getPort()+" "+msg);
                write_buffer.clear();
                write_buffer.put(charset.encode(msg));
                write_buffer.flip();
                //将数据写入缓冲区之后再将数据全部放到每一个合理的客户端的channel中;
                while(write_buffer.hasRemaining()){
                    otherClient.write(write_buffer);
                }
            }
        }
    }

    public static void main(String[] args) {
        new ChatServer(8888).start();
    }
}

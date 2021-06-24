package AIO.Server;

import simple.Client.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wengyinbing
 * @data 2021/6/24 19:48
 **/
public class ChatServer {
    private int port;
    private static final Integer BUFFER = 1024;

    private Charset charset = Charset.forName("UTF-8");

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;

    //在线用户列表，为了保证程序在并发下的线程安全
    //在写的时候加锁，读的时候不加锁，因为在进行转发消息的时候需要频繁读取用户列表，利用这各客户channel进行写消息
    private CopyOnWriteArrayList<ClientHandler> clientHandlerList;

    public ChatServer(int port) {
        this.port = port;
        clientHandlerList=new CopyOnWriteArrayList<>();
    }

    public void start(){
        /*
        建立一个固定的线程池
        将channelgroup与他进行绑定，这相当于是系统资源
         */
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            serverSocketChannel=AsynchronousServerSocketChannel.open(channelGroup);
            serverSocketChannel.bind(new InetSocketAddress("127.0.0.1",port));
            System.out.println("服务器启动：端口【"+port+"】");
            /**
             * AIO中accept可以异步调用，就用上面说到的CompletionHandler方式
             * 第一个参数是辅助参数，回调函数中可能会用上的，如果没有就填null;第二个参数为CompletionHandler接口的实现
             * 这里使用while和System.in.read()的原因：
             * while是为了让服务器保持运行状态，前面的NIO，BIO都有用到while无限循环来保持服务器运行，但是它们用的地方可能更好理解
             * System.in.read()是阻塞式的调用，只是单纯的避免无限循环而让accept频繁被调用，无实际业务功能。
             */
            while (true) {
                serverSocketChannel.accept(null, new AcceptHandler());
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //群发信息
    private void sendMessage(AsynchronousSocketChannel clientChannel,String msg){
        for(ClientHandler c:clientHandlerList){
            if(!c.clientChannel.equals(clientChannel)){
                ByteBuffer buffer = charset.encode(msg);
                //因为直接将buffer交给channel就可以了，没有其他的操作，所以辅助为nuyll，而read操作需要将buffer进行群发
                c.clientChannel.write(buffer,null,c);
            }
        }
    }

    //根据客户端通道获取端口号
    private  String getPort(AsynchronousSocketChannel socketChannel){
        try{
            InetSocketAddress address = (InetSocketAddress) socketChannel.getRemoteAddress();
            return "客户端["+address.getPort()+"]:";
        } catch (IOException e) {
            e.printStackTrace();
            return  "客户端[Undefined]:";
        }
    }

    //移除客户端
    private void removeClientHandle(ClientHandler handler){
        clientHandlerList.remove(handler);
        System.out.println(getPort(handler.clientChannel)+"断开连接。。。");
        try{
            if(handler.clientChannel!=null){
                handler.clientChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //两个参数 第一个参数是成功accept返回的类型，第二个参数是辅助参数，
    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel,Object>{

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
            if(serverSocketChannel.isOpen()){
                //serversocketchannel通过异步调用的方法accept，返回的是连接的客户端socketchannel，两个传输参数，第一个是辅助参数
                //一般传入handler中进行进一步处理，没有的话就放null
                serverSocketChannel.accept(null,this);
            }
            if(clientChannel != null && clientChannel.isOpen()){

                //把用户添加到 在线用户列表中
                ClientHandler clientHandler = new ClientHandler(clientChannel);
                System.out.println(getPort(clientChannel)+"上线啦！");
                clientHandlerList.add(clientHandler);
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
                //使用异步处理从客户端收到的数据
                //三个参数，第一个参数是存放接收到客户端数据的数据容器，第二个参数要交给handler进行群发消息的操作
                clientHandler.clientChannel.read(buffer,buffer,clientHandler);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接失败 "+exc);
        }
    }

    private class ClientHandler implements CompletionHandler<Integer, ByteBuffer>{
        private AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if(buffer!=null){
                if(result < 0){
                    //如果有数据传输过来，但是read结果小于0，说明客户端通道channel出现异常
                    //做下线处理
                    removeClientHandle(this);
                }
                //进行数据的群发
                buffer.flip();
                String msg = String.valueOf(charset.decode(buffer));
                sendMessage(this.clientChannel,getPort(this.clientChannel) + msg);

                //服务端打印消息
                System.out.println(getPort(this.clientChannel) + msg);

                buffer = ByteBuffer.allocate(BUFFER);
                if(msg.equals("quit")){
                    removeClientHandle(this);
                }
                else{
                    //继续监听这个channel上数据的read
                    clientChannel.read(buffer,buffer,this);
                }
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            System.out.println("客户端读写异常 "+exc);
        }
    }

    public static void main(String[] args) {
        new ChatServer(8888).start();
    }
}

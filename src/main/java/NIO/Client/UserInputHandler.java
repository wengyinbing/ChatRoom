package NIO.Client;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author wengyinbing
 * @data 2021/6/23 16:46
 **/
//用户输入必须建立一个单独的线程，因为不知道什么时候会输入
public class UserInputHandler implements  Runnable {
    private ChatClent client;
    public UserInputHandler(ChatClent client){
        this.client = client;
    }

    @Override
    public void run() {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                String msg = reader.readLine();
                client.send(msg);
                if(msg.equals("quit")){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

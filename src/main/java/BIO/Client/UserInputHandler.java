package BIO.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author wengyinbing
 * @data 2021/6/22 21:57
 **/
public class UserInputHandler implements  Runnable{
    private ChatClient client;
    public UserInputHandler(ChatClient c){
        client = c;
    }
    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            //String msg = null;
            while (true) {
                //System.out.println("请输入：");
                String input = reader.readLine();
                client.sendMessage(input);
                if(input.equals("quit")){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

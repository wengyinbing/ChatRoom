package simple.Client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author wengyinbing
 * @data 2021/6/22 20:43
 **/
public class Client {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8888;
        try(Socket socket = new Socket(host,port)){
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //用户输入
            BufferedReader userWriter = new BufferedReader(new InputStreamReader(System.in));

            while(true){
                System.out.println("请输入： ");
                String s = userWriter.readLine();
                writer.write(s+"\n");
                writer.flush();

                System.out.println(reader.readLine());
                if(s.equals("quit")){
                    break;
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

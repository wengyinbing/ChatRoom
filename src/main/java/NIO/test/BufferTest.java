package NIO.test;

import java.nio.IntBuffer;
import java.util.Random;

/**
 * @author wengyinbing
 * @data 2021/6/24 10:47
 **/
public class BufferTest {
    private IntBuffer intBuffer;

    public static void main(String[] args) {
        IntBuffer intBuffer = IntBuffer.allocate(10);
        System.out.println(intBuffer);
        for(int i=0;i<5;i++){
            intBuffer.put(new Random().nextInt(10));
            //System.out.println(intBuffer);
        }
        intBuffer.flip();
        System.out.println(intBuffer);
        while(intBuffer.hasRemaining()){
            System.out.println(intBuffer.get());
        }
        System.out.println(intBuffer);
        intBuffer.clear();
        System.out.println(intBuffer);
        while(intBuffer.hasRemaining()){
            System.out.println(intBuffer.get());
        }
    }
}

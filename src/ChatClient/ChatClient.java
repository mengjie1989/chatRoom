package ChatClient;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.*;

import SocketUtilities.MyClientSocket;
import SocketUtilities.MyServerSocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by refuhoo on 4/25/17.
 */

public class ChatClient extends Thread {
    public static int portNumber = 8888;
    private MyClientSocket mClientSocket = null;
    private SocketChannel channel = null;
    listenThread mListenThread = null;

    @Override
    public void run () {
        try {
            mClientSocket = new MyClientSocket();
            mClientSocket.connectToPort(portNumber);
            mListenThread = new listenThread();
            mListenThread.start();
        } catch (Exception e) {
            System.out.println("Error when connecting.");
            System.out.println(e);
            return;
        }
        while (true) {
            try {
                String str = getUserInput();
                if (str != null && str.length() != 0) {
                    mClientSocket.write(str);
                }
            } catch (IOException e) {
                System.out.println("Error when writing. " + e);
            }
        }

//        try {
//            mClientSocket.close();
//        } catch (IOException e) {
//            System.out.println("Error when closing. " + e);
//        }
    }

    private String getUserInput() {

        String input = System.console().readLine();

        if ("q".equals(input)) {
            System.out.println("Exit!");
            System.exit(0);
        }

        return input;
    }

    private class listenThread extends Thread {
        @Override
        public void run () {
            while (true) {
                try {
                    String str = mClientSocket.read(1000);
                    if (str != null && str.length() != 0 && !str.equals("Success: Silent")) {
                        System.out.println(str);
                    }
                } catch (IOException e) {
                    System.out.println("Error when reading. " + e);
                }

            }
        }
    }

    public static void main(String[] args) {
        try {
            ChatClient client = new ChatClient();
            client.start();
            client.join();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error when creating");
        }

        return;
    }
}

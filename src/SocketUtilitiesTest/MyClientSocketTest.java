package SocketUtilitiesTest;

import SocketUtilities.MyClientSocket;
import SocketUtilities.MyServerSocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by refuhoo on 4/22/17.
 */
public class MyClientSocketTest extends Thread {
    public static int portNumber = 2222;
    private MyClientSocket clientSocket = null;
    private SocketChannel channel = null;

    @Override
    public void run () {
        try {
            clientSocket = new MyClientSocket();
            clientSocket.connectToPort(portNumber);
            clientSocket.write("Request 1.");
            //clientSocket.write("Request 2.");
        } catch (Exception e) {
            System.out.println("Error when connecting.");
            System.out.println(e);
            return;
        }
        while (true) {
            try {
                String str = clientSocket.read(100);
                if (str != null) {
                    System.out.println("Got Ack: " + str);
                }
            } catch (IOException e) {
                System.out.println(e);
                System.out.println("Error when running");
                break;
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error when closing");
        }
    }

    public static void main(String[] args) {
        try {
            MyClientSocketTest myClientSocketTest = new MyClientSocketTest();
            myClientSocketTest.start();
            myClientSocketTest.join();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error when creating");
        }

        return;
    }
}

package SocketUtilitiesTest;

import SocketUtilities.MyClientSocket;
import SocketUtilities.MyServerSocket;
import SocketUtilities.Utilities;

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
// Testing
public class MyServerSocketTest extends Thread {
    public static int portNumber = 2222;
    private MyServerSocket serverSocket = null;
    private ArrayList<SocketChannel> socketChannels = new ArrayList<>();

    private void writeAccpetAck(SocketChannel channel) throws IOException {
        Utilities.writeToChannel("Ack: accepted connection.", channel);
    }
    @Override
    public void run () {
        try {
            serverSocket = new MyServerSocket();
            serverSocket.openPort(portNumber);
        } catch (IOException e) {
            System.out.println("Error when initialiazing");
            System.out.println(e);
            return;
        }
        while (true) {
            try {
                SelectionKey key = null;
                key = serverSocket.acceptSocketChannel();
                if(key != null) {
                    System.out.println("Accepted a new channel connection.");
                    SocketChannel channel = (SocketChannel)key.channel();
                    socketChannels.add(channel);
                    writeAccpetAck(channel);
                }

                ArrayList<SelectionKey> keys = serverSocket.getPendingRequests();
                if (keys != null) {
                    for (SelectionKey theKey : keys) {
                        SocketChannel channel = (SocketChannel)theKey.channel();
                        String request = Utilities.readFromChannel(channel, 1000);
                        if (request == null) {
                            System.out.println("Bytes Read -1. Channel closed.");
                            channel.close();
                        } else {
                            System.out.println("Got request: " + request);
                            String str = "Ack: " + request;
                            Utilities.writeToChannel("Ack: " + request, channel);
                        }
                    }
                }
                //sleep(1000);
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Error when running");
                break;
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error when closing");
        }
    }

    public static void main(String[] args) {
        try {
            MyServerSocketTest myServerSocket = new MyServerSocketTest();
            myServerSocket.start();
            myServerSocket.join();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error when creating");
        }

        return;
    }
}
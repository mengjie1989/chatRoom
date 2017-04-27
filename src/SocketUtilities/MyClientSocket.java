package SocketUtilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by refuhoo on 4/19/17.
 */
public class MyClientSocket {
    private SocketChannel mSocketChannel;
    private Selector mSelector;

    public MyClientSocket() throws IOException {
        mSocketChannel = SocketChannel.open();
    }

    public SocketChannel getmSocketChannel() {
        return mSocketChannel;
    }

    public void connectToPort (int portNumber) throws IOException {
        System.out.println("Connecting to port " + portNumber);
        mSocketChannel.connect(new InetSocketAddress(portNumber));
        mSocketChannel.configureBlocking(false);
        System.out.println("Connected");
    }

    public long read (ByteBuffer buffer) throws IOException {
        return Utilities.readFromChannel(buffer, mSocketChannel);
    }

    public String read (int capacity) throws IOException {
        return Utilities.readFromChannel(mSocketChannel, capacity);
    }

    public void write (ByteBuffer buffer) throws IOException {
        Utilities.writeToChannel(buffer, mSocketChannel);
    }

    public void write (String str) throws IOException {
        Utilities.writeToChannel(str, mSocketChannel);
    }

    public void close() throws IOException {
        mSocketChannel.close();
    }
}

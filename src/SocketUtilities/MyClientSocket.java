package SocketUtilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;

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
        mSelector = Selector.open();
        mSocketChannel.register(mSelector, SelectionKey.OP_READ);
        System.out.println("Connected");
    }

    public void waitToRead() throws IOException {
        int readyChannels = mSelector.select();

        if(readyChannels == 0) {
            System.out.println("Strange select result 1.");
        }

        Set<SelectionKey> selectedKeys = mSelector.selectedKeys();
        if (selectedKeys.size() != 1) {
            System.out.println("Strange select result 2.");
        }
        selectedKeys.clear();
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

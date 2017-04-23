package SocketUtilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by refuhoo on 4/19/17.
 */
public class MyServerSocket {
    private ServerSocketChannel mServerSocketChannel;
    private Selector mSelector;

    public ServerSocketChannel openPort (int portNumber) throws IOException {
        mServerSocketChannel = ServerSocketChannel.open();
        mServerSocketChannel.socket().bind(new InetSocketAddress(portNumber));
        mServerSocketChannel.configureBlocking(false);
        mSelector = Selector.open();
        System.out.println("Server bind to InetSocketAddress " + new InetSocketAddress(portNumber) + " port " + portNumber);
        //mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);

        return mServerSocketChannel;
    }

    public SelectionKey acceptSocketChannel() throws IOException {
        SelectionKey key = null;
        SocketChannel socketChannel = mServerSocketChannel.accept();
        if (socketChannel != null) {
            System.out.println("Got a new connection");
            socketChannel.configureBlocking(false);
            key = socketChannel.register(mSelector, SelectionKey.OP_READ);
        }

        return key;
    }

    public ArrayList<SelectionKey> getPendingRequests() throws IOException {
        int readyChannels = mSelector.selectNow();

        if(readyChannels == 0) {
            return null;
        }

        Set<SelectionKey> selectedKeys = mSelector.selectedKeys();
        ArrayList<SelectionKey> copiedKeys = new ArrayList<SelectionKey>(selectedKeys);
        selectedKeys.clear();

        return copiedKeys;
    }

    public void close() throws IOException {
        mServerSocketChannel.close();
    }
}

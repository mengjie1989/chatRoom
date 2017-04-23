package ChatDaemon;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import SocketUtilities.MyServerSocket;
/*
public class ChatDaemon extends Thread {

    public static int portNumber = 8888;

    private static int defaultServerNum = 4;
    private int serverNum;

    private ArrayList<ChatRoom> roomList = new ArrayList<ChatRoom>();
    private ArrayList<ServerThread> serverPool = new ArrayList<ServerThread>();
    private HashMap<Integer, User> userMap = new HashMap<Integer, User>();
    private HashSet<Socket> clientSockets = new HashSet<Socket>();
    private ServerSocket daemonSocket;

    ChatDaemon(ServerSocket daemonSocket) {
        this(daemonSocket, defaultServerNum);
    }

    ChatDaemon(ServerSocket daemonSocket, int serverNum) {
        this.daemonSocket = daemonSocket;
        this.serverNum = serverNum;
    }

    @Override
    public void run () {
        for (int i=0; i<serverNum; i++) {
            serverPool.add(new ServerThread());
            serverPool.get(i).start();
        }

        while (true) {
            try {
                daemonSocket.accept();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    // Find server thread with minimum unfinished requests in queue.
    private ServerThread chooseServerThread () {
        int min = Integer.MAX_VALUE;
        ServerThread retval;

        for (int i=0; i<serverNum; i++) {
            ServerThread curThread = serverPool.get(i);
            int requestNum = curThread.getRequestQueueSize();
            if (requestNum == 0) {
                return curThread;
            } else {
                if (requestNum < min) {
                    min = requestNum;
                    retval = curThread;
                }
            }
        }

    }

    public static void main(String[] args) {
        ChatDaemon chatDaemon;
        ServerSocket daemonSocket;

        try {
            daemonSocket = new ServerSocket(portNumber);
            chatDaemon = new ChatDaemon(daemonSocket);
            chatDaemon.start();
            chatDaemon.join();
            if (!daemonSocket.isClosed()) {
                daemonSocket.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return;
    }

    class ChatRoom {
        int roomNumber;
        ArrayList<User> userList;
    }

    class User {
        int userID;
        String userName;
    }

    class ServerThread extends Thread {
        int getRequestQueueSize(void);
    }
}

*/
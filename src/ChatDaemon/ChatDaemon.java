package ChatDaemon;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.net.ServerSocket;

import SocketUtilities.MyClientSocket;
import SocketUtilities.MyServerSocket;
import SocketUtilities.Utilities;

public class ChatDaemon extends Thread {

    public static int portNumber = 8888;

    private static int defaultServerNum = 4;
    private int serverNum;

    private ArrayList<Room> mRoomList = new ArrayList<Room>();
    private ArrayList<ServerThread> mServerPool = new ArrayList<ServerThread>();
    private HashMap<Integer, User> mUserMap = new HashMap<Integer, User>();
    private HashSet<Socket> mClientSockets = new HashSet<Socket>();
    private MyServerSocket mDaemonSocket;

    ChatDaemon(int serverNum) {
        this.serverNum = serverNum;
    }

    private boolean initialize () {
        try {
            mDaemonSocket = new MyServerSocket();
            mDaemonSocket.openPort(portNumber);
            for (int i = 0; i < serverNum; i++) {
                mServerPool.add(new ServerThread());
                mServerPool.get(i).start();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Chat Daemon initialization failure.");
            return false;
        }
    }

    @Override
    public void run () {
        try {
            if (initialize() == false)
                return;
            while (true) {
                SelectionKey key = null;
                key = mDaemonSocket.acceptSocketChannel();
                if(key != null) {
                    System.out.println("Accepted a new channel connection.");
//                        SocketChannel channel = (SocketChannel)key.channel();
//                        socketChannels.add(channel);
//                        writeAccpetAck(channel);
                }

                ArrayList<SelectionKey> keys = mDaemonSocket.getPendingRequests();
                if (keys != null) {
                    for (SelectionKey theKey : keys) {
                        SocketChannel channel = (SocketChannel)theKey.channel();
                        String request = Utilities.readFromChannel(channel, 1000);
                        ServerThread threadToProcess = chooseServerThread();
                        if (request == null) {
                            threadToProcess.addRequest(new Request("connection_down", theKey));
                            channel.close();
                        } else {
                            threadToProcess.addRequest(new Request(request, theKey));
                        }
                    }
                }
                sleep(1000);
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error when running");
//            if (mDaemonSocket != null) {
//                mDaemonSocket.close();
//            }
        }
    }

    // Find server thread with minimum unfinished requests in queue.
    private ServerThread chooseServerThread () {
        int min = Integer.MAX_VALUE;
        ServerThread retval = null;

        for (int i=0; i<serverNum; i++) {
            ServerThread curThread = mServerPool.get(i);
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

        return retval;
    }

    public static void main(String[] args) {
        try {
            ChatDaemon chatDaemon = new ChatDaemon(defaultServerNum);
            chatDaemon.start();
            chatDaemon.join();
        } catch (Exception e) {
            System.out.println(e);
        }

        return;
    }

    void addRoomToList(Room newRoom) {
        synchronized (mRoomList) {
            mRoomList.add(newRoom);
        }
    }

    Room getRoom(int roomID) {
        synchronized (mRoomList) {
            if (roomID >= mRoomList.size())
                return null;
            else
                return mRoomList.get(roomID);
        }
    }

    void sendErrorResponse(SocketChannel channel, String message) {
        try {
            Utilities.writeToChannel("Error: " + message, channel);
        } catch (IOException e) {
            System.out.println(e + "Error: " + message);
        }
    }

    void sendSuccessResponse(SocketChannel channel, String message) {
        try {
            Utilities.writeToChannel("Success: " + message, channel);
        } catch (IOException e) {
            System.out.println(e + "Success: " + message);
        }
    }

    private void registerUser(SelectionKey key, String payload) {
        User user = (User) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        String name = payload;

        if (user != null) {
            sendErrorResponse(channel, "You already registered.");
            return;
        }
        if (name == null || name.length() == 0) {
            sendErrorResponse(channel, "Provide a valid name.");
            return;
        }

        user = User.createUser(name);
        if (user == null) {
            sendErrorResponse(channel, "Registration failed");
            return;
        }

        user.setChannel(channel);
        key.attach(user);

        sendSuccessResponse(channel, name + " registered with ID " +user.getUserID());
    }

    private void deregisterUser(SelectionKey key) {
        User user = (User) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();

        if (user == null) {
            sendErrorResponse(channel, "You haven't registered.");
            return;
        }

        user.exitRoom();
        key.attach(null);

        sendSuccessResponse(channel, "You have de-registered.");
    }

    private void createRoom(SelectionKey key) {
        User user = (User) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();

        if (user == null) {
            sendErrorResponse(channel, "You haven't registered.");
            return;
        }

        user.exitRoom();
        Room newRoom = Room.createRoom();

        if (newRoom == null) {
            sendErrorResponse(channel, "Create room failed.");
            return;
        }

        user.enterRoom(newRoom);
        addRoomToList(newRoom);

        sendSuccessResponse(channel, "Room created with room ID " + newRoom.getRoomId());
    }

    private void changeName(SelectionKey key, String payload) {
        User user = (User) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        String name = payload;

        if (user == null) {
            sendErrorResponse(channel, "You haven't registered.");
            return;
        }

        user.setName(name);

        sendSuccessResponse(channel, "Name changed to " + name);
    }

    private void enterRoom(SelectionKey key, String payload) {
        User user = (User) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        int roomID = 0;

        if (user == null) {
            sendErrorResponse(channel, "You haven't registered.");
            return;
        }

        try {
            roomID = Integer.valueOf(payload);
        } catch (NumberFormatException e) {
            sendErrorResponse(channel, "Please enter valid room number.");
            return;
        }

        Room room = getRoom(roomID);
        if (room == null) {
            sendErrorResponse(channel, "Room hasn't been created.");
            return;
        }

        user.exitRoom();
        user.enterRoom(room);

        sendSuccessResponse(channel, "Entered room " + roomID);
    }

    private void exitRoom(SelectionKey key) {
        User user = (User) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();

        if (user == null) {
            sendErrorResponse(channel, "You haven't registered.");
            return;
        }

        user.exitRoom();

        sendSuccessResponse(channel, "Exited room.");
    }

    private void sendMessage(SelectionKey key, String payload) {
        User user = (User) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        String message = payload;

        if (user == null) {
            sendErrorResponse(channel, "You haven't registered.");
            return;
        }

        Room room = user.getRoom();

        if (room == null) {
            sendErrorResponse(channel, "Please enter a room.");
            return;
        }

        room.broadcastMessage(user, message);

        sendSuccessResponse(channel, "Silent");
    }

    private void closeConnection(SelectionKey key) {
        User user = (User) key.attachment();

        if (user == null)
            return;

        user.exitRoom();
        key.attach(null);
    }

    class ServerThread extends Thread {
        SyncQueue<Request> requestQueue = new SyncQueue<Request>();

        public int getRequestQueueSize() {
            return requestQueue.getSize();
        }

        public void addRequest(Request request) {
            requestQueue.add(request);
        }

        private Request getRequest() {
            return requestQueue.poll();
        }

        @Override
        public void run () {
            while (true ) {
                Request request = getRequest();
                if (request != null) {
                    processRequest(request);
                }
            }
        }

        private void processRequest(Request request) {
            synchronized (request.key) {
                SelectionKey key = request.key;
                String payload = request.requestPayload;

                switch (request.requestType) {
                    case REGISTER_USER:
                        registerUser(key, payload);
                        break;
                    case DEREGISTER_USER:
                        deregisterUser(key);
                        break;
                    case CHANGE_NAME:
                        changeName(key, payload);
                        break;
                    case CREATE_ROOM:
                        createRoom(key);
                        break;
                    case ENTER_ROOM:
                        enterRoom(key, payload);
                        break;
                    case EXIT_ROOM:
                        exitRoom(key);
                        break;
                    case CONNECTION_DOWN:
                        closeConnection(key);
                        break;
                    default:
                    case SEND_MESSAGE:
                        sendMessage(key, payload);
                        break;
                }
            }
        }
    }
}

package ChatDaemon;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Semaphore;

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
    SyncQueue<Request> mRequestQueue = new SyncQueue<Request>();
    private Semaphore mRequestSemaphore = new Semaphore(0, false);

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
                }

                ArrayList<SelectionKey> keys = mDaemonSocket.getPendingRequests();
                if (keys != null) {
                    for (SelectionKey theKey : keys) {
                        SocketChannel channel = (SocketChannel)theKey.channel();
                        String requestStr = Utilities.readFromChannel(channel, 1000);
                        if (requestStr == null) {
                            mRequestQueue.add(new Request("connection_down", theKey));
                            channel.close();
                            mRequestSemaphore.release();
                        } else {
                            String[] requests = requestStr.split("///");
                            for (String request : requests) {
                                mRequestQueue.add(new Request(request, theKey));
                            }
                            mRequestSemaphore.release(requests.length);
                        }
                    }
                }
                sleep(1000);
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error when running");
        }
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

    private void getRoomInfo(SelectionKey key) {
        User user = (User) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();

        if (user == null) {
            sendErrorResponse(channel, "You haven't registered.");
            return;
        }

        Room room = user.getRoom();

        if (room == null) {
            sendErrorResponse(channel, "Please enter a room first.");
            return;
        }

        String roomInfo = room.getUserInfo();

        sendSuccessResponse(channel, roomInfo);

    }

    class ServerThread extends Thread {
        private Request getRequest() {
            return mRequestQueue.poll();
        }

        @Override
        public void run () {
            while (true ) {
                try {
                    mRequestSemaphore.acquire();
                    Request request = getRequest();
                    if (request == null) {
                        System.out.println("Failed to fetch request!");
                    } else {
                        processRequest(request);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
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
                    case GET_ROOMINFO:
                        getRoomInfo(key);
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

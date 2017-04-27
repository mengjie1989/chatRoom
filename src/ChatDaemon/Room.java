package ChatDaemon;

import SocketUtilities.Utilities;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by refuhoo on 4/23/17.
 */
public class Room {
    private static int nextRoomID = 0;
    private static int maxRooms = 1000;
    public synchronized static Room createRoom() {
        if (nextRoomID >= maxRooms) {
            return null;
        } else {
            return new Room(nextRoomID++);
        }
    }

    private HashSet<User> mUsers;
    private int mRoomID;
    private Object mLock = new Object();

    public Room(int roomID) {
        mRoomID = roomID;
        mUsers = new HashSet<>();
    }

    public int getRoomId() {
        return mRoomID;
    }

    public void enterUser(User user) {
        synchronized (mLock) {
            mUsers.add(user);
        }
    }

    public void exitUser(User user) {
        synchronized (mLock) {
            mUsers.remove(user);
        }
    }

    public String getUserInfo() {
        String retval;
        synchronized (mLock) {
            retval = "Room " + getRoomId() + " has " + mUsers.size()
                    + " users:\n";
            Iterator<User> iterator = mUsers.iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                retval += user.getName() + " " + user.getUserID() + "\n";
            }
        }

        return retval;
    }
    
    public void broadcastMessage(User sender, String message) {
        synchronized (mLock) {
            if (!mUsers.contains(sender)) {
                System.out.println("User not in room. Can't send message");
                return ;
            }
            
            String senderName = sender.getName();
            int senderId = sender.getUserID();
            message = senderName + " " + senderId + ": " + message;

            Iterator<User> iterator = mUsers.iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                SocketChannel channel = user.getChannel();
                try {
                    Utilities.writeToChannel(message, channel);
                } catch (IOException e) {
                    System.out.println("Failed to send message " + message + " to channel " + channel);
                }
            }
        }
    }


}

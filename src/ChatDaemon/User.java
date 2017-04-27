package ChatDaemon;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by refuhoo on 4/23/17.
 */
class User {
    private static int nextUserID = 0;
    private static int maxUsers = 10000;
    public synchronized static User createUser(String name) {
        if (nextUserID >= maxUsers) {
            return null;
        } else {
            return new User(nextUserID++, name);
        }
    }

    private int     mUserID;
    private Room    mRoom;
    private String  mName;
    private Object  mLock = new Object();
    private SocketChannel mChannel;

    public User(int userID, String name) {
        mUserID = userID;
        mName = name;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public int getUserID() { return mUserID; }

    public Room getRoom() { return mRoom; }

    public void setChannel(SocketChannel channel) {
        mChannel = channel;
    }

    public SocketChannel getChannel() {
        return mChannel;
    }

    public void enterRoom(Room room) {
        synchronized (mLock) {
            exitRoom();
            mRoom = room;
            mRoom.enterUser(this);
        }
    }

    public void exitRoom() {
        synchronized (mLock) {
            if (mRoom != null) {
                mRoom.exitUser(this);
                mRoom = null;
            }
        }
    }
}

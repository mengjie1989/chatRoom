package ChatDaemon;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by refuhoo on 4/24/17.
 */
public class Request {
    static String REQUEST_SPLITTER = ";;;";

    enum RequestType {
        REGISTER_USER,
        DEREGISTER_USER,
        CHANGE_NAME,
        CREATE_ROOM,
        EXIT_ROOM,
        ENTER_ROOM,
        SEND_MESSAGE,
        CONNECTION_DOWN
    }

    public RequestType  requestType;
    public String       requestPayload;
    public SelectionKey key;
    public String       requestStr;

    public Request (String str, SelectionKey key) {
        this.key = key;
        this.requestStr = str;
        parseRequest(str);
    }

    private void parseRequest(String str) {
        String[] parts = str.split(REQUEST_SPLITTER);
        if (parts.length == 0)
            return;
        if (parts.length == 2)
            requestPayload = parts[1];

        switch (parts[0]) {
            case "register":
                requestType = RequestType.REGISTER_USER;
                break;
            case "deregister":
                requestType = RequestType.DEREGISTER_USER;
                break;
            case "change_name":
                requestType = RequestType.CHANGE_NAME;
                break;
            case "create_room":
                requestType = RequestType.CREATE_ROOM;
                break;
            case "exit_room":
                requestType = RequestType.EXIT_ROOM;
                break;
            case "enter_room":
                requestType = RequestType.ENTER_ROOM;
                break;
            case "connection_down":
                requestType = RequestType.CONNECTION_DOWN;
                break;
            default:
                requestType = RequestType.SEND_MESSAGE;
                requestPayload = str;
                break;
        }
    }
}

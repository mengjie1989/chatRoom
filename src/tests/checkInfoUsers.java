package tests;

import java.util.Random;
import ChatClient.ChatClient;

/**
 * Created by refuhoo on 4/26/17.
 */


class checkinfoUsers extends ChatClient {
    static int i;
    protected String[] args;
    public checkinfoUsers(String[] args) { this.args = args; }

    @Override
    protected String getUserInput() {
        String name = args[0];
        if (i++==0)
            return "register;;;"+name+"///enter_room;;;0";

        try {
            int ran = new Random().nextInt(50) + 1;
            sleep(ran);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "get_roominfo";

    }

    public static void main(String[] args) {
        try {
            checkinfoUsers client = new checkinfoUsers(args);
            client.start();
            client.join();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error when creating");
        }

        return;
    }
}
package tests;

import java.util.Random;
import ChatClient.ChatClient;

/**
 * Created by refuhoo on 4/26/17.
 */


class chattyUser extends ChatClient {
    static int i;
    protected String[] args;
    public chattyUser(String[] args) { this.args = args; }

    @Override
    protected String getUserInput() {
        String name = args[0];
        if (i++==0)
            return "register;;;"+name+"///enter_room;;;0";

        try {
            int ran = new Random().nextInt(50) + 200;
            sleep(ran);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return args[1];

    }

    public static void main(String[] args) {
        try {
            chattyUser client = new chattyUser(args);
            client.start();
            client.join();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error when creating");
        }

        return;
    }
}
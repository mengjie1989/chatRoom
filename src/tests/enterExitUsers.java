package tests;

import ChatClient.ChatClient;

import java.util.Random;

/**
 * Created by refuhoo on 4/26/17.
 */

class enterExitUsers extends ChatClient {
    static int i;
    protected String[] args;
    public enterExitUsers(String[] args) { this.args = args; }

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
        if (i % 2 == 0) {
            return "exit_room";
        } else {
            return "enter_room;;;0";
        }

    }

    public static void main(String[] args) {
        try {
            enterExitUsers client = new enterExitUsers(args);
            client.start();
            client.join();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error when creating");
        }

        return;
    }
}
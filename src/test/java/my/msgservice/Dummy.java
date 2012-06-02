package my.msgservice;

import java.util.concurrent.TimeUnit;

public class Dummy {

    public static void main(String[] args) {
        new Dummy();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException x) {
            x.printStackTrace();
        }
    }

    public Dummy() {
        createDispatcher();
    }

    private void createDispatcher() {
        MessageDispatcher<String> md = new MessageDispatcher<String>();

        for (int i = 0; i < 10; ++i) {
            md.send("Hello", null, null, i + 1, TimeUnit.SECONDS);
        }

        System.out.println("Added 10 messages");

        md.setAutoDispatch(true);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException x) {
            x.printStackTrace();
        }

        md.setAutoDispatch(false);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException x) {
            x.printStackTrace();
        }

        md.setAutoDispatch(true);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException x) {
            x.printStackTrace();
        }

    }
}

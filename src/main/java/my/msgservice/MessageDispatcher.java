package my.msgservice;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * MessageDispatcher
 *
 * @author david
 *
 */
public final class MessageDispatcher<V> {

    private DelayQueue<DelayedMessage<V>> messageQueue = new DelayQueue<DelayedMessage<V>>();

    private Thread poller;
    private volatile boolean autoDispatch;

    public MessageDispatcher() {
        this(false);
    }

    public MessageDispatcher(boolean autoDispatch) {
        setAutoDispatch(autoDispatch);
    }

    public void setAutoDispatch(boolean autoDispatch) {
        this.autoDispatch = autoDispatch;
        if (autoDispatch && poller == null && !messageQueue.isEmpty()) {
            poller = new Poller();
            poller.start();
        }
    }

    public void send(V message, Object sender, MessageReceiver<V> receiver) {
        send(message, sender, receiver, 0, TimeUnit.NANOSECONDS);
    }

    public void send(V message, Object sender, MessageReceiver<V> receiver, long delay, TimeUnit timeUnit) {
        DelayedMessage<V> msg = new DelayedMessage<V>(message, sender, receiver, delay, timeUnit);
        if (delay <= 0) {
            msg.send();
        } else {
            messageQueue.offer(msg);
            if (autoDispatch && poller == null) {
                poller = new Poller();
                poller.start();
            }
        }
    }

    public void sendPendingMessages() {
        DelayedMessage<V> msg = null;
        while ((msg = messageQueue.poll()) != null) {
            msg.send();
        }
    }

    private class Poller extends Thread {

        @Override
        public void run() {
            while (autoDispatch && !messageQueue.isEmpty()) {
                try {
                    DelayedMessage<V> msg = messageQueue.poll(1, TimeUnit.SECONDS);
                    if (msg != null) {
                        msg.send();
                    }
                } catch (InterruptedException e) {
                }
            }
            poller = null;
        }
    }

}

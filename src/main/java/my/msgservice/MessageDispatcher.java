package my.msgservice;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * MessageDispatcher
 *
 * @author david
 *
 */
public final class MessageDispatcher {

    /**
     * The message queue
     */
    private DelayQueue<DelayedMessage<?>> messageQueue = new DelayQueue<DelayedMessage<?>>();

    /**
     * Dispatcher thread that handles auto dispatch
     */
    private Dispatcher dispatcher;

    /**
     * Whether auto dispatch is turned on or off
     */
    private volatile boolean autoDispatch;

    /**
     * Create a new MessageDispatcher. Same as {@code MessageDispatcher(false)}
     */
    public MessageDispatcher() {
        this(false);
    }

    /**
     * Create a new MessageDispatcher.
     * @param autoDispatch true if messages should be automatically dispatched when they expire, false otherwise.
     * If false, delayed messages are dispatched by calling {@see sendPendingMessages}
     */
    public MessageDispatcher(boolean autoDispatch) {
        setAutoDispatch(autoDispatch);
    }

    /**
     * @param autoDispatch true if messages should be automatically dispatched when expired, false otherwise.
     */
    public void setAutoDispatch(boolean autoDispatch) {
        this.autoDispatch = autoDispatch;
        if (autoDispatch && dispatcher == null && !messageQueue.isEmpty()) {
            dispatcher = new Dispatcher();
            dispatcher.start();
        }
    }

    /**
     * Send a message immediately
     * @param message the message
     * @param sender the sender
     * @param receiver the receiver
     */
    public <V> void send(V message, Object sender, MessageReceiver<V> receiver) {
        send(message, sender, receiver, 0, TimeUnit.NANOSECONDS);
    }

    /**
     * Enqueue a delayed message
     * @param message the message
     * @param sender the sender
     * @param receiver the receiver
     * @param delay how long before the message should be dispatched
     * @param timeUnit the delay time unit
     */
    public <V> void send(V message, Object sender, MessageReceiver<V> receiver, long delay, TimeUnit timeUnit) {
        DelayedMessage<V> msg = new DelayedMessage<V>(message, sender, receiver, delay, timeUnit);
        if (delay <= 0) {
            msg.send();
        } else {
            messageQueue.offer(msg);
            if (autoDispatch && dispatcher == null) {
                dispatcher = new Dispatcher();
                dispatcher.start();
            }
        }
    }

    /**
     * Dispatch all expired messages
     */
    public void sendPendingMessages() {
        DelayedMessage<?> msg = null;
        while ((msg = messageQueue.poll()) != null) {
            msg.send();
        }
    }

    /**
     * Private dispatcher thread.
     * Only lives as long as autoDispatch is true and there are messages in the message queue.
     */
    private class Dispatcher extends Thread {

        @Override
        public void run() {
            while (autoDispatch && !messageQueue.isEmpty()) {
                try {
                    DelayedMessage<?> msg = messageQueue.poll(1, TimeUnit.SECONDS);
                    if (msg != null) {
                        msg.send();
                    }
                } catch (InterruptedException e) {
                }
            }
            dispatcher = null;
        }
    }

}

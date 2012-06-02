package my.msgservice;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DelayedMessage<V> implements Delayed {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayedMessage.class);

    private V message;
    private Object sender;
    private MessageReceiver<V> receiver;
    private long deliveryTime;

    public DelayedMessage(V message, Object sender, MessageReceiver<V> receiver, long delay, TimeUnit timeUnit) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.deliveryTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(delay, timeUnit);
    }

    public void send() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending " + message + " from " + sender + " to " + receiver);
        }
        if (receiver != null) {
            receiver.handleMessage(message, sender);
        }
    }

    @Override
    public long getDelay(TimeUnit timeUnit) {
        return timeUnit.convert(deliveryTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        long thisDelay = getDelay(TimeUnit.NANOSECONDS);
        long thatDelay = other.getDelay(TimeUnit.NANOSECONDS);
        if (thisDelay < thatDelay) {
            return -1;
        } else if (thisDelay > thatDelay) {
            return 1;
        }
        return 0;
    }
}

package my.msgservice;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class TestMessageDispatcher {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    MessageDispatcher messageDispatcher;
    MessageReceiver<String> foo, bar;

    @Before
    public void setUp() {
        messageDispatcher = new MessageDispatcher();
        foo = mock(MessageReceiver.class);
        bar = mock(MessageReceiver.class);
    }

    @Test
    public void testSend() {
        messageDispatcher.send("Hello", foo, bar);

        // foo should not handle message
        verify(foo, never()).handleMessage(any(String.class), any());

        // bar should
        verify(bar).handleMessage("Hello", foo);
    }

    @Test
    public void testSendDelayed() {
        int delay = 1000;

        // send delayed message
        messageDispatcher.send("Hello", foo, bar, delay, TimeUnit.MILLISECONDS);

        // call sendPending after delay -> should dispatch message!
        sendPending(delay);

        // message should not have been dispatched yet!
        verify(bar, never()).handleMessage(any(String.class), any());

        // message should be dispatched within 'delay'.
        verify(bar, timeout(delay)).handleMessage("Hello", foo);
    }

    @Test
    public void testSendDelayedMulti() {
        int delay = 100;
        int times = 10;

        // send delayed message
        for (int i = 0; i < times; ++i) {
            messageDispatcher.send("Hello", foo, bar, delay, TimeUnit.MILLISECONDS);
        }

        // call sendPending after delay -> should dispatch messages!
        sendPending(delay);

        // messages should not have been dispatched yet!
        verify(bar, never()).handleMessage(any(String.class), any());

        // messages should be dispatched after 'delay'.
        verify(bar, timeout(delay).times(times)).handleMessage("Hello", foo);
    }

    @Test
    public void testSendDelayedTooLong() {
        int delay = 1000;

        // send delayed message
        messageDispatcher.send("Hello", foo, bar, delay * 2, TimeUnit.MILLISECONDS);

        // call sendPending after delay -> should NOT dispatch message!
        sendPending(delay);

        // message should NOT have been dispatched within 'delay'.
        verify(bar, timeout(delay).never()).handleMessage(any(String.class), any());
    }

    @Test
    public void testAutoDispatch() {
        messageDispatcher.setAutoDispatch(true);
        int delay = 100;
        int times = 10;

        for (int i = 0; i < times; ++i) {
            messageDispatcher.send("Hello", foo, bar, i * delay, TimeUnit.MILLISECONDS);
        }

        verify(bar, timeout(times * delay).times(times)).handleMessage("Hello", foo);
    }

    private void sendPending(long millis) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                messageDispatcher.sendPendingMessages();
            }
        }, millis, TimeUnit.MILLISECONDS);
    }

}

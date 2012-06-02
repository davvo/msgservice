package my.msgservice;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class TestMessageDispatcher {

    MessageDispatcher<String> messageDispatcher;
    MessageReceiver<String> foo, bar;

    @Before
    public void setUp() {
        messageDispatcher = new MessageDispatcher<String>();
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
        triggerSendPending(delay);

        // message should not have been dispatched yet!
        verify(bar, never()).handleMessage(any(String.class), any());

        // message should be dispatched within 'delay'.
        verify(bar, timeout(delay)).handleMessage("Hello", foo);
    }

    @Test
    public void testSendDelayedMulti() {
        int delay = 1000;
        int times = 10;

        // send delayed message
        for (int i = 0; i < times; ++i) {
            messageDispatcher.send("Hello", foo, bar, delay, TimeUnit.MILLISECONDS);
        }

        // call sendPending after delay -> should dispatch messages!
        triggerSendPending(delay);

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

        // call sendPending after delay -> should dispatch message!
        triggerSendPending(delay);

        // message should NOT have been dispatched within 'delay'.
        verify(bar, timeout(delay).never()).handleMessage(any(String.class), any());
    }

    @Test
    public void testAutoDispatch() {
        messageDispatcher.setAutoDispatch(true);
        int times = 10;

        for (int i = 0; i < times; ++i) {
            messageDispatcher.send("Hello", foo, bar, i * 100, TimeUnit.MILLISECONDS);
        }

        verify(bar, timeout(1000).times(times)).handleMessage("Hello", foo);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception x) {
            // Oops
        }
    }

    /**
     * This method will call MessageDispatcher.sendPending after a delay.
     *
     * @private
     */
    private void triggerSendPending(final long delay) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                sleep(delay);
                messageDispatcher.sendPendingMessages();
            }

        }).start();
    }
}

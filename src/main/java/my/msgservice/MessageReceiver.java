package my.msgservice;

public interface MessageReceiver<V> {

    public void handleMessage(V message, Object sender);

}

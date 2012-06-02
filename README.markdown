## A simple java message dispatcher

**Implement MessageReceiver**
    
    public class MyMessageReceiver implements MessageReceiver<String> {
  
        @Override
        public void handleMessage(String message, Object sender) {
            System.out.println("Got message " + message + " from " + sender);
        }
        
        ...
        
    }

**Start sending messages**

    MessageDispatcher messageDispatcher = new MessageDispatcher(true);
    MyMessageReceiver receiver = new MyMessageReceiver();
    ...
    messageDispatcher.send("Hello", this, receiver);

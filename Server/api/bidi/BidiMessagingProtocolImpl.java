package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.messages.Logout;
import bgu.spl.net.srv.messages.Message;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {
    private boolean shouldTerminate=false;
    private int connectionId;
    private ConnectionsImpl<T> connections;

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl<T>) connections;
    }

    @Override
    public void process(T message) { //gets bytes array
        ((Message)message).process(connectionId);
        if(message instanceof Logout)
            if(((Logout)message).logoutSucceeded())
                shouldTerminate = true;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

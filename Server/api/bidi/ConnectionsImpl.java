package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.ConnectionHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    public int getCounter() {
        return counter;
    }

    private int counter=0;
    private ConcurrentHashMap<Integer, ConnectionHandler> connectionHandlers;
    private ConcurrentHashMap<ConnectionHandler, Integer> clientsId;

    private static class ConnectionsHolder{
        private static ConnectionsImpl Connections_instance = new ConnectionsImpl();
    }

    public static ConnectionsImpl getInstance()
    {
        return ConnectionsHolder.Connections_instance;
    }

    private ConnectionsImpl(){
        connectionHandlers = new ConcurrentHashMap<>();
        clientsId = new ConcurrentHashMap<>();
    }

    public int addClient(ConnectionHandler<T> connectionHandler) {
        //Put if absent instead of put?
        int connectionId = counter;
        counter++;
        connectionHandlers.put(connectionId,connectionHandler);
        clientsId.put(connectionHandler,connectionId);
        return connectionId;
    }

    @Override
    //Do we need to Sync?
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> ch = connectionHandlers.get(connectionId);
        if(ch!=null) {
            ch.send(msg);
            return true;
        }
        return false;
    }

    //Do we need to Sync?
    @Override
    public void broadcast(T msg) {
        for (Map.Entry<Integer,ConnectionHandler> ch :connectionHandlers.entrySet()) {
            ConnectionHandler thisCH = ch.getValue();
            if(thisCH!=null)
                thisCH.send(msg);
        }
    }
    //Do we need to Sync?
    @Override
    public void disconnect(int connectionId) {
        connectionHandlers.remove(connectionId);

    }

    public int getIdByHandler(ConnectionHandler ch){
        return clientsId.get(ch);
    }


}

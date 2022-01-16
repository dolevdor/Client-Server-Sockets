package bgu.spl.net.srv.messages;

import bgu.spl.net.srv.User;

import java.util.LinkedList;

public class Block extends Message{
    private String Username;

    public Block(String Username) {
        this.Username = Username;
    }

    public void process(int connectionId){
        this.clientID = connectionId;
        if(!getDatabase().isUserExist(Username))
            sendError((short) 12);
        else{
            User thisUser = getDatabase().getUserByUserConnectionId(clientID);
            User toBlock = getDatabase().getUserByUserName(Username);
            thisUser.addToAmBlocking(toBlock);
            thisUser.removeFollowing(toBlock);
            sendAck((short)12);
        }
    }
}

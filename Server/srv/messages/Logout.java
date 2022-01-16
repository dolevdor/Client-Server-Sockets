package bgu.spl.net.srv.messages;

import bgu.spl.net.srv.User;

public class Logout extends Message{
    private boolean logOutSucceeded=false;

    public Logout() {
    }
//jj
    public boolean logoutSucceeded(){
        return logOutSucceeded;
    }

    public void process(int connectionId){
        this.clientID = connectionId;
        if(!getDatabase().isUserExist(clientID)) {
            sendError((short) 3);
        }
        else {
            User user = getDatabase().getUserByUserConnectionId(clientID);
            user.setLoggedIn(false);

            logOutSucceeded=true;
            sendAck((short) 3);
        }
    }
}

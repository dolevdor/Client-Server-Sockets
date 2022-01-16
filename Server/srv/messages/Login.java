package bgu.spl.net.srv.messages;

import bgu.spl.net.srv.User;
import bgu.spl.net.srv.messages.Message;

import java.security.KeyPair;

public class Login extends Message {
    private String Username;
    private String Password;
    private String Captcha;

    public Login(String Username,String Password,String Captcha) {
        this.Username=Username;
        this.Password=Password;
        this.Captcha=Captcha;
    }

    public void process(int connectionId){
        this.clientID = connectionId;
        if(!getDatabase().isUserExist(Username) || !Captcha.equals("1")) {
            sendError((short) 2);
        }
        else {
            User user = getDatabase().getUserByUserName(Username);
            if(!(user.getPassword()).equals(Password))
                sendError((short) 2);
            else {
                user.setLoggedIn(true);
                getDatabase().removeUser(user.getUsername());
                user.setConnectionId(connectionId);
                getDatabase().addUser(user);
                //add the user to the login database(update the register to be logged in)
                sendAck((short) 2);
                while(!user.getNotificationsList().isEmpty()) {
                    byte[] notificationToSend = user.getNotificationsList().poll();
                    getConnections().send(clientID,notificationToSend);
                }
            }
        }
    }

}

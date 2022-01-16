package bgu.spl.net.srv;

import bgu.spl.net.srv.messages.Message;

import javax.xml.crypto.Data;
import java.util.LinkedList;
import java.util.function.LongUnaryOperator;

public class User {
    private String username;
    private String password;
    private short age;
    private boolean loggedIn;
    private Database database;
    private int connectionId;
    private LinkedList<User> followers;
    private LinkedList<User> following;
    private LinkedList<byte[]> messagesSent;
    private LinkedList<User> blockedBy;
    private LinkedList<User> amBlocking;
    private LinkedList<byte[]> notifciationList;

    public String getPassword() {
        return password;
    }

    public User(String username, String password, short age, int connectionId){
        this.username = username;
        this.password=password;
        this.age = age;
        loggedIn = false;
        this.connectionId = connectionId;
        following = new LinkedList<>();
        followers = new LinkedList<>();
        messagesSent = new LinkedList<>();
        blockedBy = new LinkedList<>();
        amBlocking = new LinkedList<>();
        notifciationList = new LinkedList<>();
    }

    public void setLoggedIn(boolean bool){
        loggedIn = bool;
    }

    public boolean getLoggedIn(){
        return loggedIn;
    }

    private void addFollowMe(User user) {
        followers.add(user);
    }

    public void addToFollow(User user){
        if (!following.contains(user))
            following.add(user);
        if (!user.getFollowers().contains(this))
            user.addFollowMe(this);
    }

    public void addToMessages(byte[] msg){
        notifciationList.add(msg);
    }

    public void addToAmBlocking(User user){
        if(!this.getAmBlocking().contains(user)){
            this.amBlocking.add(user);
            this.removeFollowing(user);
        }
        if(!user.getBlockedBy().contains(this)) {
            user.addBlockingMe(this);
            user.removeFollowing(this);
        }

    }
    public LinkedList<User> getAmBlocking(){
        return amBlocking;
    }
    public int getConnectionId(){
        return connectionId;
    }

    public short getAge(){
        return age;
    }

    public short getFollowersSize(){
        return (short)followers.size();
    }

    public short getMessagesSize(){
        return (short)messagesSent.size();
    }

    public LinkedList<User> getFollowing(){
        return following;
    }

    public LinkedList<User> getFollowers(){
        return followers;
    }

    public short getFollowingSize(){
        return (short)following.size();
    }

    public void addMessageSent(byte[] message){
        messagesSent.add(message);
    }

    public LinkedList<User> getBlockedBy(){
        return blockedBy;
    }

    private void addBlockingMe(User user) {
        blockedBy.add(user);
    }

    public String getUsername(){
        return this.username;
    }

    public void removeFollowing(User user){
        this.following.remove(user);
        user.removeFollower(this);
    }

    private void removeFollower(User user){
        this.followers.remove(user);
    }

    public void addNotification(byte[] notification){
        notifciationList.add(notification);
    }

    public LinkedList<byte[]> getNotificationsList(){
        return notifciationList;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }
}

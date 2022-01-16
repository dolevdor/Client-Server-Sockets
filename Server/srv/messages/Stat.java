package bgu.spl.net.srv.messages;

import bgu.spl.net.srv.User;
import bgu.spl.net.srv.messages.Message;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class Stat extends Message {

    private int msgLen = 0;//1KB
    byte[] msgToSend = new byte[12];
    private String listOfUsernames;
    private LinkedList<String> actualListOfUsernames = new LinkedList<>();

    public Stat(String listOfUsernames){
        this.listOfUsernames = listOfUsernames;
    }

    public void process(int connectionId){
        this.clientID = connectionId;
        createList();
        if (!isUserNameLoggedIn(getDatabase().getUserByUserConnectionId(clientID).getUsername())||
                !isUserNameRegister(getDatabase().getUserByUserConnectionId(clientID).getUsername()))
                        sendError((short)8);
        else{
            for (String user : actualListOfUsernames){
                if(getDatabase().isUserExist(user)) {
                    User user1 = getDatabase().getUserByUserName(user);
                    if (!user1.getBlockedBy().contains(getDatabase().getUserByUserConnectionId(clientID)) &&
                            !user1.getAmBlocking().contains(getDatabase().getUserByUserConnectionId(clientID))) {
                        msgToSend = new byte[12];
                        byte[] byteMsg = encode(user);
                        getConnections().send(clientID, byteMsg);
                    }
                }
                else
                    sendError((short)8);
            }
//            sendAck((short)8);
        }
    }

    public byte[] encode(String username){
            User user = getDatabase().getUserByUserName(username);
            short ackCode = 10;
            short opCode = 8;
            short age = user.getAge();
            short numPosts = user.getMessagesSize();
            short numOfFollowers = user.getFollowersSize();
            short numFollowing = user.getFollowingSize();
            byte[] opBytes = shortToBytes(opCode);
            byte[] ackBytes = shortToBytes(ackCode);
            byte[] ageBytes = shortToBytes(age);
            byte[] numPostsBytes = shortToBytes(numPosts);
            byte[] numOfFollowersBytes = shortToBytes(numOfFollowers);
            byte[] numFollowingBytes = shortToBytes(numFollowing);
            pushByte(ackBytes[0]);
            pushByte(ackBytes[1]);

            pushByte(opBytes[0]);
            pushByte(opBytes[1]);

            pushByte(ageBytes[0]);
            pushByte(ageBytes[1]);

            pushByte(numPostsBytes[0]);
            pushByte(numPostsBytes[1]);

            pushByte(numOfFollowersBytes[0]);
            pushByte(numOfFollowersBytes[1]);

            pushByte(numFollowingBytes[0]);
            pushByte(numFollowingBytes[1]);

            msgLen=0;

        return msgToSend;
        }

    public void createList(){
        String username="";
        for (int i = 0; i < listOfUsernames.length(); i++){
            char c = listOfUsernames.charAt(i);
            if (c != '|'){
                username = username + c;
                if(i == listOfUsernames.length()-1)
                    actualListOfUsernames.add(username);
            }
            else{
                actualListOfUsernames.add(username);
                username="";
            }
        }
    }

    public void pushByte(byte nextByte) {
//        if (msgLen >= msgToSend.length)
//            msgToSend = Arrays.copyOf(msgToSend, msgLen * 2);
        msgToSend[msgLen] = nextByte;
        msgLen++;
    }
}

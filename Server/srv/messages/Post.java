package bgu.spl.net.srv.messages;

import bgu.spl.net.srv.User;
import bgu.spl.net.srv.messages.Message;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class Post extends Message {

    private String content;
    private int msgLen = 0;
    byte[] msgToSend = new byte[1<<5];//1KB

    public Post(String content) {
        this.content=content;
    }


    public LinkedList<String> getUsers(){
        LinkedList<String> users = new LinkedList<>();
        int i = 0;
        char a = content.charAt(i);
        String user = "";
        while (i < content.length()){
            if (!Character.toString(a).equals("@")) { // Make sure == works that way
                i++;
                if (i != content.length())
                a = content.charAt(i);
            }
            else{
                    while (!Character.isWhitespace(a) && i < content.length()-1){ // Make sure == works that way
                        i++;
                        a = content.charAt(i);
                        if (!Character.isWhitespace(a))
                        user = user + a;
                      }
//                    if (i == content.length()-1 && !Character.isWhitespace(content.charAt(i)))
//                        user = user + content.charAt(i);

                    users.add(user);
                    user = "";
            }
        }
        return users;
    }

    public void process(int connectionId){
        this.clientID = connectionId;

        if (!(getDatabase().isUserExist(clientID))||
            !(getDatabase().getUserByUserConnectionId(clientID).getLoggedIn()))
                sendError((short)5);
        else{
            LinkedList<User> followersUsers = getDatabase().getUserByUserConnectionId(clientID).getFollowers();
            for (User user : followersUsers) {
                    byte[] byteMsg = encoder();//holds content
                    int connectionId1 = user.getConnectionId();
                    if(user.getLoggedIn())
                         getConnections().send(connectionId1, byteMsg);
                    else
                        user.addToMessages(byteMsg);

            }
            LinkedList<String> taggedUsers = getUsers();
            for (String user : taggedUsers){
                if(getDatabase().isUserExist(user)) {
                    User user1 = getDatabase().getUserByUserName(user);
                    if (!user1.getBlockedBy().contains(getDatabase().getUserByUserConnectionId(clientID))
                            && !user1.getFollowing().contains(getDatabase().getUserByUserConnectionId(clientID))) {
                        byte[] byteMsg = encoder();
                        int connectionId1 = user1.getConnectionId();
                        if(user1.getLoggedIn())
                            getConnections().send(connectionId1, byteMsg);
                        else
                            user1.addToMessages(byteMsg);
                    }
                }
            }
            sendAck((short)5);
        }
    }

    public byte[] encoder(){
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            User thisClientId = getDatabase().getUserByUserConnectionId(clientID);
            String thisUserName = thisClientId.getUsername();
            byte[] sender_Bytes = thisUserName.getBytes(StandardCharsets.UTF_8);
            int counter=5;

            short opCode = 9;
            byte[] opBytes = shortToBytes(opCode);
            pushByte(opBytes[0]);
            pushByte(opBytes[1]);

            pushByte((byte)0);//Post message

            for(byte b: sender_Bytes){
                pushByte(b);
                counter++;
            }
            pushByte((byte)'\0');

            for(byte b: contentBytes){
                pushByte(b);
                counter++;
            }
            pushByte((byte)'\0');

            byte[] shorterMsg = new byte[counter];
            for(int i=0;i<counter;i++)
                shorterMsg[i]=msgToSend[i];


//            String s = new String(msgToSend, StandardCharsets.UTF_8);
//            System.out.println(s);

            return shorterMsg;
        }

    public void pushByte(byte nextByte) {
        if (msgLen >= msgToSend.length)
            msgToSend = Arrays.copyOf(msgToSend, msgLen * 2);
        msgToSend[msgLen] = nextByte;
        msgLen++;
    }

}

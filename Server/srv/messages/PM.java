package bgu.spl.net.srv.messages;

import bgu.spl.net.srv.User;
import bgu.spl.net.srv.messages.Message;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PM extends Message {

    private String Username;
    private String content;
    private String sending_date_and_time;
    private int receiverId;
    private int msgLen = 0;//1KB
    byte[] msgToSend = new byte[1<<5];

    public PM(String Username, String content,String sending_date_and_time) {
        this.Username = Username;
        this.content = content;
        this.sending_date_and_time = sending_date_and_time;
        User user = getDatabase().getUserByUserName(Username);
        receiverId = user.getConnectionId();
    }



    public boolean isUserReceiverRegister(){
        return getDatabase().isUserExist(receiverId);
    }

    public boolean theSenderFollowReceiver(){
        return getDatabase().isUserExist(clientID);
    }

    public void process(int connectionId){
        this.clientID = connectionId;
        if(isUserReceiverRegister()==false||theSenderFollowReceiver()==false
        || getDatabase().getUserByUserConnectionId(receiverId).getAmBlocking().contains(getDatabase().getUserByUserConnectionId(clientID)))
        { //the server should send ERROR
            sendError((short) 6);
        }
        else {
            byte[] byteMsg = encoder();
            User receiverUser = getDatabase().getUserByUserConnectionId(receiverId);
            if (receiverUser.getLoggedIn()) {
                getConnections().send(receiverId, byteMsg);
                getDatabase().getUserByUserConnectionId(clientID).addMessageSent(byteMsg);
            } else {
                receiverUser.addToMessages(byteMsg);
            }
            sendAck((short)6);
        }
    }

    //create notification
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

       pushByte((byte)1);//PM message

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

        return shorterMsg;
    }

    public void pushByte(byte nextByte) {
        if (msgLen >= msgToSend.length)
            msgToSend = Arrays.copyOf(msgToSend, msgLen * 2);
        msgToSend[msgLen] = nextByte;
        msgLen++;
    }


}

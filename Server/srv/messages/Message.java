package bgu.spl.net.srv.messages;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.srv.Database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class Message {
    private byte[] bytes;
    private int len;
    private byte[] partBytes;

    protected int clientID;

    private ConnectionsImpl connections;
    private Database database;
    public ConnectionsImpl getConnections() {
        return connections;
    }

    public Database getDatabase() { return database; }

    public Message(){
        bytes = new byte[1 << 10]; // 1KB byte array
        len = 0;
        connections = connections.getInstance();
        database = database.getInstance();

    }

    public boolean isUserNameRegister(String username){
        return database.isUserExist(username);
    }

    public boolean isUserNameLoggedIn(String username){
        return database.getUserByUserName(username).getLoggedIn();
    }


    public void pushByte(byte nextByte) {
        if (len >= partBytes.length)
            partBytes = Arrays.copyOf(partBytes, len * 2);
        partBytes[len] = nextByte;
        len++;
    }


    public String popString() {
        String result = new String(partBytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
     //   count++;
        partBytes = new byte[1 << 10];
        return result;
    }

    public byte popByte(){
        byte result = partBytes[0];
        len=0;
      //  count++;
        partBytes = new byte[1 << 10];
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public void sendError(short msgOpCode){
        byte[] msgOp = shortToBytes(msgOpCode);
        byte[] msg = new byte[4];// = {0,11,0,1}
        msg[0] = 0;
        msg[1] = 11;
        msg[2] = 0;
        msg[3] = msgOp[1];
        getConnections().send(clientID, msg);
    }

    public void sendAck(short msgOpCode){
        byte[] msgOp = shortToBytes(msgOpCode);
        byte[] msg = new byte[4];
        msg[0]=0;
        msg[1]=10;
        msg[2]=0;
        msg[3]=msgOp[1];

       // byte[] msg = {0,10,0,msgOpCode};
//        byte[] msg = new byte[4];// = {0,10,0,1}
//        msg[0] = shortToBytes(OpCode)[0];
//        msg[1] = shortToBytes(OpCode)[1];
//        msg[2] = shortToBytes(msgOpCode)[0];
//        msg[3] = shortToBytes(msgOpCode)[1];
        getConnections().send(clientID, msg);
//        System.out.println("sendAck, print only msg: "+msg);
    }

    public abstract void process(int connectionId);

}


package bgu.spl.net.api;

import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.srv.messages.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class MessageEncoderDecoderImpl<Message> implements MessageEncoderDecoder<Message>{
   private boolean first=true;
//    private boolean second=false;
//    private byte[] opCode = new byte[2];
    private short OP;
    private ByteBuffer opcode = ByteBuffer.allocate(2); // saves the opcode (type) of message
//    private int start=1;
    private int len=0;

    private byte[] bytes;
    private byte[] partBytes = new byte[1 << 10];
    protected int clientID;
    private ConnectionsImpl connections;
    private String Username;
    private String Password;
    private String birthday;
    private byte follow;

    private String Captcha;
    private boolean byteTime=false;

    private String[] badWords = {"shit", "shit!", "shit?", "shit.", "shit,"};
    private String content;//Post
    private String sending_date_and_time;//PM

    String listOfUsernames;//stat

    int registerCount=0;

    @Override
    public Message decodeNextByte(byte nextByte) {
        if (!opcode.hasRemaining()) {

//            if (!first || !second)
//                decodeOp(nextByte);
//            String a="43534534534 454 \0\0\0\0\0";
//            a.split("(\0)+");
//            String [] b =
            switch ((int) OP) {
                case 1:
                    return decRegister(nextByte);
                case 2:
                    return decLogin(nextByte);
                case 3:
                    return decLogout(nextByte);
                case 4:
                    return decFollow(nextByte);
                case 5:
                    return decPost(nextByte);
                case 6: ;
                    return decPM(nextByte);
                case 7:
                    if (nextByte == (byte) ';') {
                        pushByte(nextByte);
                        popByte();
                        initialize();
                        return (Message) new Logstat();
                    }
                    break;
                case 8:
                    return decStat(nextByte);
                case 12:
                    return decBlock(nextByte);
            }
        }

        else {
            opcode.put(nextByte);
            if (!opcode.hasRemaining()) { //we read 2 bytes and therefore can take care the specific message
                opcode.flip();
                OP = opcode.getShort(); // read the next 2 bytes from position - get the type of message
            }
                }
        return null;
    }

//    public void decodeOp(byte b) {
//        if (first == false) {
//            opCode[0] = b;
//            first = true;
//        } else {
//            if (second == false) {
//                opCode[1] = b;
//                second = true;
//                short ans = bytesToShort(opCode);
//                OP=ans;
//            }
//        }
//    }

//    public short bytesToShort(byte[] byteArr)
//    {
//        short result = (short)((byteArr[0] & 0xff) << 8);
//        result += (short)(byteArr[1] & 0xff);
//        return result;
//    }

    @Override
    public byte[] encode(Object message) {
        return new byte[0];
    }

    public Message decLogout(byte nextByte){
        if (nextByte == (byte) ';') {
            pushByte(nextByte);
            popByte();
            initialize();
            return (Message) new Logout();
        }
        return null;
    }

    public Message decRegister(byte nextByte) {
        if (nextByte == (byte) ';') {
            pushByte(nextByte);
            popByte();
            initialize();
            return (Message) new Register(Username,Password,birthday);
        }

        if (nextByte == '\0') {
            if (registerCount == 0) {//UserName
                Username = popString();
 //               Username = Username.substring(1);
//                System.out.println("username:"+Username);
            }
            else if (registerCount == 1) {//Password
                Password = popString();
//                System.out.println("PASS:"+Password);
            }
            else if (registerCount == 2)  {
                birthday = popString();
//                System.out.println(birthday);
                registerCount=0;
            }
        }
        else{
            pushByte(nextByte);
        }
        return null;
    }


    public Message decLogin(byte nextByte) {
        if (nextByte == (byte) ';') {
            pushByte(nextByte);
            popByte();
            initialize();
            return (Message) new Login(Username,Password,Captcha);
        }
        if (nextByte == '\0') {
            if (registerCount== 0) {//UserName
                Username = popString();
//                System.out.println("Login login: " + Username);
            }
            else if (registerCount == 1) {//Password
                Password = popString();
//                System.out.println("Password login: " + Password);
            }
        }
        else{
            if (registerCount == 2) {
                pushByte(nextByte);
                Captcha = popString();
//                System.out.println("Captch login: " + Captcha);
            }
            else
            pushByte(nextByte);
        }
        return null;
    }

    public Message decFollow(byte nextByte) {
        if (nextByte == (byte) ';') {
            pushByte(nextByte);
            popByte();
            initialize();
            return (Message) new Follow(follow,Username);
        }
        if(first) {
            pushByte(nextByte);
            follow = popByte();
            first=false;
        }
        else {
            if (nextByte == '\0') {
                Username = popString();

            }
            else
                pushByte(nextByte);
        }
        return null;
    }

    public Message decPost(byte nextByte) {
        if (nextByte == (byte) ';') {
            pushByte(nextByte);
            popByte();
            initialize();
            return (Message) new Post(content);
        }
        if (nextByte == '\0') {
            content = popString();
        }
        pushByte(nextByte);
        return null;
    }

    public Message decPM(byte nextByte) {
        if (nextByte == (byte) ';') {
            pushByte(nextByte);
            popByte();
            initialize();
            return (Message) new PM(Username,content,sending_date_and_time);
        }
        if (nextByte == '\0')  {
            if (registerCount == 0) {//UserName
                Username = popString();
            }
            else if (registerCount == 1) {//Content
                content = popString();
                String[] s = content.split(" ");
                for (String badWord : badWords) {
                    for (String word : s)
                    if (word.equals(badWord))
                        content = content.replace(word, "<filtered>");
                }
            }
            else if (registerCount == 3)
                sending_date_and_time = popString();

        }
        else {
            pushByte(nextByte);
        }
        return null;
    }

    public Message decBlock(byte nextByte) {
        if (nextByte == (byte) ';') {
            pushByte(nextByte);
            popByte();
            initialize();
            return (Message) new Block(Username);
        }
        if ((char)(nextByte&0xFF) == '\0')  {
            Username = popString();

        }
        pushByte(nextByte);
        return null;
    }

    public Message decStat(byte nextByte) {
        if (nextByte == (byte) ';') {
            pushByte(nextByte);
            popByte();
            initialize();
            return (Message) new Stat(listOfUsernames);
        }
        if (nextByte == '\0') {
            listOfUsernames = popString();

        }
        pushByte(nextByte);
        return null;
    }




    public String popString() {
        String result = new String(partBytes, 0, len, StandardCharsets.UTF_8);
       // start=len;
        len = 0;
        registerCount++;
        partBytes = new byte[1 << 10];
        return result;
    }

    public byte popByte(){
        byte result = partBytes[0];
        len=0;
     //   registerCount++;
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

    public void pushByte(byte nextByte) {
        if (len >= partBytes.length)
            partBytes = Arrays.copyOf(partBytes, len * 2);
        partBytes[len] = nextByte;
        len++;
    }

    public void initialize(){
        opcode.clear();
        len = 0;
        registerCount = 0;
        first = true;
    }

}
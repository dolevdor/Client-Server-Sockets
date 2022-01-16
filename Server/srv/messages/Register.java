package bgu.spl.net.srv.messages;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Database;
import bgu.spl.net.srv.User;
import bgu.spl.net.srv.messages.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Register extends Message {

    private String Username;
    private String Password;
    private String birthday;


    public Register(String Username,String Password,String birthday) {
        this.Username = Username;
        this.Password = Password;
        this.birthday = birthday;
//        System.out.println("nana:"+Username);
//        System.out.println("nana2:"+Password);
//        System.out.println("nana3:"+birthday);

       // clientID = getConnections().addClient(new ConnectionHandler<T>);
    }

    public void process(int connectionId){
//        System.out.println("SOMEONE CALL REGISTER TO PROCESS");
        this.clientID = connectionId;
        Database database = getDatabase();
        if(database.isUserExist(Username))
            sendError((short) 1);
        else{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate localDate = LocalDate.parse(birthday, formatter);
            LocalDate today = LocalDateTime.now().toLocalDate();
            short age = (short)Period.between(localDate, today).getYears();
            User user = new User(Username,Password,age,connectionId);
            database.addUser(user);
            sendAck((short)1);
        }
        //server check if in the database this username is already in use
        //Enter to the database
    }



}

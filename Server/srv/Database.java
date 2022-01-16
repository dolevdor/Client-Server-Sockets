package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private ConcurrentHashMap<String, User> usersByUserName;
    private ConcurrentHashMap<Integer, User> usersByConnectionId;

    private static class DatabaseHolder{
        private static Database Database_instance = new Database();
    }

    public static Database getInstance()
    {
        return DatabaseHolder.Database_instance;
    }

    private Database(){
        usersByUserName = new ConcurrentHashMap<>();
        usersByConnectionId = new ConcurrentHashMap<>();
    }

    public boolean isUserExist(String username){
        return usersByUserName.containsKey(username);
    }

    public boolean isUserExist(int connectionId){
        return usersByConnectionId.containsKey(connectionId);
    }

    public User getUserByUserName(String username){
        return usersByUserName.get(username);
    }

    public User getUserByUserConnectionId(int connectionId){
        return usersByConnectionId.get(connectionId);
    }

    public void addUser(User user){
        usersByUserName.put(user.getUsername(), user);
        usersByConnectionId.put(user.getConnectionId(), user);
    }

    public void removeUser(String username){
        User user = usersByUserName.get(username);
        if (user != null) {
            usersByUserName.remove(user);
            usersByConnectionId.remove(user);
        }
    }

    public void updateConId(User user){
        int conId = usersByConnectionId.get(user.getConnectionId()).getConnectionId();
        usersByConnectionId.remove(conId);
        usersByConnectionId.put(user.getConnectionId(), user);
    }

    public ConcurrentHashMap<String, User> getMapByUserName(){
        return usersByUserName;
    }

}

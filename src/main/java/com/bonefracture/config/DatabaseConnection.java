package com.bonefracture.config;

import java.sql.Connection;

public class DatabaseConnection {
    private Connection connection;
    private DatabaseSettings settings;

    private DatabaseConnection(){

    }

    private static class Holder{
        private static final DatabaseConnection INSTANCE=new DatabaseConnection();
    }
    public static DatabaseConnection getInstance(){
        return Holder.INSTANCE;
    }
    public void connect(DatabaseSettings settings){
        //TODO
    }
    public void disconnect(){

    }
    public boolean isConnected(){
        //TODO
        return false;
    }
    public Connection getConnection()
    {
        return connection;
    }
}

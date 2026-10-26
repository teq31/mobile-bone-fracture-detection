package com.bonefracture.config;

public class DatabaseSettings {
    private String url;
    private String username;
    private String password;
    private int poolSize;

    public DatabaseSettings(String url, String username, String password, int poolSize)
    {
        this.url=url;
        this.username=username;
        this.password=password;
        this.poolSize=poolSize;
    }

    public String getUrl(){
        return url;
    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
    public int getPoolSize(){
        return poolSize;
    }
}

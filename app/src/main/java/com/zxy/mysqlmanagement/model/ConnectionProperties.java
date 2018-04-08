package com.zxy.mysqlmanagement.model;

import java.io.Serializable;

public class ConnectionProperties implements Serializable{
    private String host;
    private String port;
    private String DBName;
    private String user;
    private String password;

    @Override
    public String toString() {
        return "ConnectionProperties{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", DBName='" + DBName + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDBName() {
        return DBName;
    }

    public void setDBName(String DBName) {
        this.DBName = DBName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

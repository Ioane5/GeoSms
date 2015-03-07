package com.ioane.sharvadze.geosms.websms;

import java.security.Security;

/**
 * Created by Ioane on 3/5/2015.
 */
public abstract  class AbstractWebSms {

    protected String accountName;

    protected String userName;
    protected String password;
    protected String cookie;
    protected int numMessages;

    /**
     * Overriding webSms client must initialize this variable
     * in static block.
     */
    protected static String LOGIN_URL;
    /**
     * Overriding webSms client must initialize this variable
     * in static block.
     */
    protected static String SEND_URL;

    protected static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36";

    public static interface FIELDS{
        String GET ="GET";
        String POST ="POST";
        String HEAD ="HEAD";

        String USER = "user";
        String PASSWORD = "password";
        String USER_AGENT = "User-agent: ";
        String SET_COOKIE = "Set-Cookie";
        String COOKIE = "Cookie";
    }

    public AbstractWebSms(String userName,String password){
        this.userName = userName;
        this.password = password;
    }


    public abstract boolean authenticate();

    public abstract boolean sendSms(String message,String address);

    public abstract int getNumMessages();

    public String getCookie(){
        return cookie;
    }

    public void setCookie(String cookie){
        this.cookie = cookie;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

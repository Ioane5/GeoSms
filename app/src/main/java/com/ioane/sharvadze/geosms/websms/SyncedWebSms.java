package com.ioane.sharvadze.geosms.websms;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ioane.sharvadze.geosms.MyPreferencesManager;

/**
 * Created by Ioane on 3/24/2015.
 */
public class SyncedWebSms implements WebSms , SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = SyncedWebSms.class.getSimpleName();

    private WebSms webSms;

    private Context context;

    public SyncedWebSms(Context context) {
        Log.i(TAG,"constructor");
        this.context = context;
        this.webSms = MyPreferencesManager.getWebSmsManager(context);
        SharedPreferences preferences = MyPreferencesManager.getWebSmsPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean authenticate() {
        if(webSms == null) return false;
        return webSms.authenticate();
    }

    @Override
    public boolean sendSms(String message, String address) {
        if(webSms == null) return false;
        return webSms.sendSms(message,address);
    }

    @Override
    public int getNumMessages() {
        if(webSms == null) return 0;
        return webSms.getNumMessages();
    }

    @Override
    public String getCookie() {
        if(webSms == null) return "";
        return webSms.getCookie();
    }

    @Override
    public void setCookie(String cookie) {
        if(webSms == null) return;
        webSms.setCookie(cookie);
    }

    @Override
    public String getPassword() {
        if(webSms == null) return "";
        return webSms.getPassword();
    }

    @Override
    public void setPassword(String password) {
        if(webSms == null) return;
        webSms.setPassword(password);
    }

    @Override
    public String getUserName() {
        if(webSms == null) return "";
        return webSms.getUserName();
    }

    @Override
    public void setUserName(String userName) {
        if(webSms == null) return;
        webSms.setUserName(userName);
    }

    @Override
    public String getAccountName() {
        if(webSms == null) return "";
        return webSms.getAccountName();
    }

    @Override
    public void setAccountName(String name) {
        if(webSms == null) return;
        webSms.setAccountName(name);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG,key);
        if(MyPreferencesManager.WEBSMS_COOKIE.equals(key))
            return;
        MyPreferencesManager.saveCookie(context,null);
        if(webSms == null) return;

        webSms.setCookie(""); // clear cookie
        if(key == null)
            return;
        if(MyPreferencesManager.WEBSMS_NAME.equals(key)){
            this.webSms = MyPreferencesManager.getWebSmsManager(context);
        }else if(MyPreferencesManager.WEBSMS_USERNAME.equals(key)){
            this.webSms.setUserName(sharedPreferences.getString(MyPreferencesManager.WEBSMS_USERNAME,""));
        }if(MyPreferencesManager.WEBSMS_PASSWORD.equals(key)){
            this.webSms.setPassword(sharedPreferences.getString(MyPreferencesManager.WEBSMS_PASSWORD, ""));
        }
    }

}

package com.ioane.sharvadze.geosms;

import android.content.SharedPreferences;
import android.util.Log;

import com.ioane.sharvadze.geosms.websms.AbstractWebSms;
import com.ioane.sharvadze.geosms.websms.MagtifunWebSms;

import java.util.prefs.PreferenceChangeListener;

/**
 * Created by Ioane on 3/5/2015.
 */
public class MyPreferencesManager {

    private static final String TAG = MyPreferencesManager.class.getSimpleName();
    private static String WEBSMS_NAME = "websms_name";
    private static String WEBSMS_USERNAME = "websms_username";
    private static String WEBSMS_PASSWORD = "websms_password";
    private static String WEBSMS_COOKIE = "websms_cookie";
    private static int MAGTIFUN_ID = 1;
    private static int GEOCELL_ID = 191;


    public static AbstractWebSms getWebSmsManager(final SharedPreferences preferences){
        int webSmsId = Integer.parseInt(preferences.getString(WEBSMS_NAME,"-1"));
        if(webSmsId == -1) return null; // user hasn't account
        if(webSmsId == MAGTIFUN_ID){
            String username = preferences.getString(WEBSMS_USERNAME,null);
            String password = preferences.getString(WEBSMS_PASSWORD,null);
            String cookie = preferences.getString(WEBSMS_COOKIE,"");

            if(username == null || password == null) return null;
            return new MagtifunWebSms(username,password,cookie);
        }else if(webSmsId == GEOCELL_ID){
            Log.w(TAG,"geocell websms is not ready");
            return null;
        }


        return null;
    }

}

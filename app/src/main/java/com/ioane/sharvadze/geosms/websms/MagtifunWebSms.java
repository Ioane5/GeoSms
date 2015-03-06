package com.ioane.sharvadze.geosms.websms;

import android.net.Uri;
import android.nfc.Tag;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.ioane.sharvadze.geosms.Constants;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Ioane on 3/5/2015.
 */
public class MagtifunWebSms extends AbstractWebSms {

    public static final String TAG = MagtifunWebSms.class.getSimpleName();

    public static final String MESSAGE_BODY = "message_body";
    public static final String SEND_ADDRESS = "recipients";
    public static final String HOST = "www.magtifun.ge";
    static {
        LOGIN_URL = "http://www.magtifun.ge/index.php?page=11&lang=ge";
        SEND_URL = "/scripts/sms_send.php";

    }

    public MagtifunWebSms(String userName, String password,String cookie) {
        super(userName, password);
        this.cookie = cookie;
        accountName = Constants.MAGTIFUN;
    }

    /**
     * This method authenticates user with username and password.
     * also sets cookie variable.
     * @return false if authentication was not successful.
     *
     */
    @Override
    public boolean authenticate() {
        Log.i(TAG,"authenticate");
        try {
            HttpClient httpclient = new DefaultHttpClient();

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
            nameValuePairs.add(new BasicNameValuePair(FIELDS.USER, userName));
            nameValuePairs.add(new BasicNameValuePair(FIELDS.PASSWORD, password));
            nameValuePairs.add(new BasicNameValuePair("remember", "on"));

            HttpPost httppost =  new HttpPost(LOGIN_URL);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httppost.setHeader(new BasicHeader("Content-type" , "application/x-www-form-urlencoded"));

            HttpResponse response = httpclient.execute(httppost);

            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                response.getEntity().writeTo(out);
                String responseString = out.toString();

                if(!parseLoginHTML(responseString)){
                    return false;
                }
                Header[] headers = response.getHeaders(FIELDS.SET_COOKIE);
                // This will find out cookie
                setCookieFromHeader(headers);

                out.close();
                return true;
                //..more logic
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        }catch (Exception e){
            Log.i(TAG,"exception");
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param html
     * @return true if login successful.
     */
    private boolean parseLoginHTML(String html){
        return  html.contains("გაგზავნილი შეტყობინებები");
    }

    private void setCookieFromHeader(Header[] cookies){
        for (int i = 0; i < cookies.length; i++) {
            String ck = cookies[i].getValue();
            if (ck.contains("User=")){
                StringTokenizer tokenizer = new StringTokenizer(ck," ");
                while (tokenizer.hasMoreTokens()){
                    String token = tokenizer.nextToken();
                    if(token.length() > 6) {
                        this.cookie = token;
                        return;
                    }
                }
            }
        }
    }

    @Override
    public boolean sendSms(String message,String address) {
        if(TextUtils.isEmpty(cookie) && !authenticate()){
            // we couldn't login.
            return false;
        }
        try {
            HttpClient httpclient = new DefaultHttpClient();

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
            nameValuePairs.add(new BasicNameValuePair(MESSAGE_BODY, message));
            nameValuePairs.add(new BasicNameValuePair(SEND_ADDRESS, address));

            HttpPost httppost =  new HttpPost(SEND_URL);
            httppost.addHeader(new BasicHeader(FIELDS.COOKIE, this.cookie));
            httppost.addHeader(new BasicHeader("Content-type" , "application/x-www-form-urlencoded"));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(new HttpHost(HOST),httppost);
            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                response.getEntity().writeTo(out);
                String responseString = out.toString();

                Log.i(TAG,responseString);

                out.close();
                return true;
                //..more logic
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        }catch (Exception e){
            Log.i(TAG,"exception");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getNumMessages() {
        return 0;
    }
}

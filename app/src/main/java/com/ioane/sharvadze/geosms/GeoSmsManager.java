package com.ioane.sharvadze.geosms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.ioane.sharvadze.geosms.objects.SMS;
import com.ioane.sharvadze.geosms.websms.SyncedWebSms;
import com.ioane.sharvadze.geosms.websms.WebSms;

import java.util.ArrayList;

import broadcastReceivers.SmsDispatcher;
import utils.Constants;

/**
 * Class GeoSmsManager
 *
 * manages sms sending
 *
 * Created by Ioane on 3/1/2015.
 */
public class GeoSmsManager {

    private static final String TAG = GeoSmsManager.class.getSimpleName();

    /**
     * Context to get content resolver.
     */
    private Context context;

    /**
     * To send web sms messages.
     */
    private WebSms webSmsManager;

    public GeoSmsManager(Context context){
        this.context = context;
        this.webSmsManager = new SyncedWebSms(context);
    }




    public void sendSms(SMS sms, String address, int threadId, Boolean web){
        new AsyncTask<Object,Void,Integer>(){
            /**
             * Executes async task of sending sms through WEB or GSM.
             * @param params SMS sms, String address,Boolean web
             * @return message resource id.
             */
            @Override
            protected Integer doInBackground(Object... params) {
                SMS sms = (SMS)params[0];
                sms.setMsgType(SMS.MsgType.PENDING);
                String address = (String)params[1];
                int threadId = (Integer)params[2];
                boolean web = (Boolean)params[3];


                ContentValues values = sms.getContentValues();
                values.put(Constants.ADDRESS,address);
                Uri insertedSmsURI = context.getContentResolver().insert(Uri.parse("content://sms/"), values);

                Intent sentPI = new Intent(Constants.Actions.MESSAGE_SENT, insertedSmsURI , context.getApplicationContext(), SmsDispatcher.class);
                sentPI.putExtra(Constants.RECIPIENT_IDS, threadId);

                if(!web){
                    // send normal sms using GSM.
                    SmsManager manager = SmsManager.getDefault();

                    ArrayList<String> dividedMessage = manager.divideMessage(sms.getText());
                    ArrayList<PendingIntent> sentPIs = new ArrayList<>(dividedMessage.size());
                    for (int i = 0; i < dividedMessage.size() ; i++) {
                        sentPIs.add(PendingIntent.getBroadcast(context,0,new Intent(sentPI),0));
                    }
                    try{
                        manager.sendMultipartTextMessage(address, null, dividedMessage, sentPIs, null);
                        Log.d(TAG,"sending");
                    }catch (Exception e){
                        Log.e(TAG,"error occured");
                        e.printStackTrace();
                    }
                }else {
                    PendingIntent pi = PendingIntent.getBroadcast(context,0,new Intent(sentPI),0);
                    // send using WIFI
                    try {
                        if(webSmsManager == null){
                            pi.send(Activity.RESULT_CANCELED);
                            return R.string.websms_not_found;
                        }
                        if(!webSmsManager.sendSms(sms.getText(),address)){
                            webSmsManager.authenticate(); // try to auth second time.
                            if(!webSmsManager.sendSms(sms.getText(),address)){
                                pi.send(Activity.RESULT_CANCELED);
                                return R.string.websms_unable_login;
                            }
                        }
                        // we sent  successfully
                        pi.send(Activity.RESULT_OK);


                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }


                }
                return null; // this return is just for Void object return type.
            }

            @Override
            protected void onPostExecute(Integer messageId) {
                super.onPostExecute(messageId);
                if(messageId != null)
                    Toast.makeText(context,messageId,Toast.LENGTH_SHORT).show();
            }
        }.execute(sms,address,threadId,web);

    }


    public void saveDraft(SMS sms){

    }

    public void deleteSms(SMS sms){

    }

    public void updateSms(SMS sms){

    }

}

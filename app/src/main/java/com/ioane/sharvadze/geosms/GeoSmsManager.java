package com.ioane.sharvadze.geosms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ListView;

import com.ioane.sharvadze.geosms.objects.SMS;

import java.util.ArrayList;

/**
 * Created by Ioane on 3/1/2015.
 */
public class GeoSmsManager {
    private ListView listView;
    private Context context;


    private static final String TAG = GeoSmsManager.class.getSimpleName();

    public GeoSmsManager(ListView listView,Context context){
        this.listView = listView;
        this.context = context;
    }


    PendingIntent pendingIntent;

    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";

    public void sendSms(SMS sms, final String address,boolean web){
        // TODO add web checking.
        new AsyncTask<SMS,Void,Void>(){
            @Override
            protected Void doInBackground(SMS... params) {
                SMS sms = params[0];
                SmsManager manager = SmsManager.getDefault();

                ContentValues values = sms.getContentValues();
                values.put(Constants.ADDRESS,address);
                Uri insertedSmsURI = context.getContentResolver().insert(Uri.parse("content://sms/"), values);

                ArrayList<String> dividedMessage = manager.divideMessage(sms.getText());
                ArrayList<PendingIntent> sentPIs = new ArrayList<PendingIntent>(dividedMessage.size());
                Intent sentPI = new Intent(Constants.Actions.MESSAGE_SENT, insertedSmsURI , context.getApplicationContext(), SmsDispatcher.class);
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



                return null;
            }

        }.execute(sms);

    }

    public void saveDraft(SMS sms){

    }

    public void deleteSms(SMS sms){

    }

    public void updateSms(SMS sms){

    }

    private class SentBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"onReceive()");
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Log.d(TAG, "SMS sent");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Log.d(TAG, "Generic failure");
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Log.d(TAG,"No service");
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Log.d(TAG, "Null PDU");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Log.d(TAG, "Radio off");
                    break;
                default:
                    Log.d(TAG,"result Code :" + getResultCode());
            }
        }
    }

}

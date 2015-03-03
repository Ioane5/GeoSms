package com.ioane.sharvadze.geosms;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.SMS;

import java.util.Objects;

/**
 * Created by Ioane on 3/1/2015.
 */
public class SmsDispatcher extends BroadcastReceiver {

    private static final String TAG = SmsDispatcher.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG,"onReceive() :" + action);
        if(action == null) return;

        if(action.equals(Constants.Actions.MESSAGE_SENT)){
            handleSmsSend(context,intent);

        }else if(action.equals(Constants.Actions.MESSAGE_DELIVERED_1) ||
                action.equals(Constants.Actions.MESSAGE_DELIVERED_2)){
            handleSmsReceive(context,intent);

        }else {
            Log.w(TAG,"unknown action "+action);
        }

    }

    private void handleSmsReceive(Context ctx,Intent intent){
        if(intent.getExtras() == null){
            Log.w(TAG,"bundle is empty");
            return;
        }
        Log.i(TAG,"sms received");
        new AsyncTask<Object,Void,Void>(){
            @Override
            protected Void doInBackground(Object... params) {
                Context ctx = (Context)params[0];
                Bundle bundle = (Bundle)params[1];
                ContentValues values = SMS.getContentValuesFromBundle(bundle);
                Log.i(TAG,"values " + values.toString());
                ctx.getContentResolver().insert(Uri.parse("content://sms/"), values);
                new Contact()
                return null;
            }
        }.execute(ctx,intent.getExtras());
    }


    private void handleSmsSend(Context ctx,Intent intent){
        Uri pendingSmsUri = intent.getData();
        if(pendingSmsUri == null){
            Log.w(TAG,"pendingSmsURI is null");
            return;
        }
        Log.i(TAG,pendingSmsUri.toString());

        new AsyncTask<Object,Void,Void>(){
            @Override
            protected Void doInBackground(Object... params) {
                Context ctx = (Context)params[0];
                Uri pendingSmsUri = (Uri)params[1];
                ContentValues values = new ContentValues();

                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Log.d(TAG, "SMS sent");
                        values.put(Constants.MESSAGE.TYPE,Constants.MESSAGE.MESSAGE_TYPE_SENT);
                        break;
                    default:
                        values.put(Constants.MESSAGE.TYPE,Constants.MESSAGE.MESSAGE_TYPE_FAILED);
                        Log.d(TAG,"Message sending failed result Code :" + getResultCode());

                }
                try{
                    ctx.getContentResolver().update(pendingSmsUri,values,null,null);
                }catch (Exception e){
                    // ignore exception
                }
                return null;
            }
        }.execute(ctx,pendingSmsUri);
    }

}

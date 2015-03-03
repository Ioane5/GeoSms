package com.ioane.sharvadze.geosms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Ioane on 3/1/2015.
 */
public class SmsDispatcher extends BroadcastReceiver {

    private static final String TAG = SmsDispatcher.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG,"onReceive() :" + action);
        if(intent.getAction().equals(Constants.Actions.MESSAGE_SENT)){
            handleSmsSend(context,intent);
        }else{
            Log.w(TAG,"unknown action "+action);
        }

    }

    private void handleSmsReceive(Context ctx,Intent intent){
        
    }


    private void handleSmsSend(Context ctx,Intent intent){
        Uri pendingSmsUri = intent.getData();
        if(pendingSmsUri == null){
            Log.w(TAG,"pendingSmsURI is null");
            return;
        }
        Log.i(TAG,pendingSmsUri.toString());

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
        ctx.getContentResolver().update(pendingSmsUri,values,null,null);



    }

}

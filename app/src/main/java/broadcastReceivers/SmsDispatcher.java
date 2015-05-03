package broadcastReceivers;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ioane.sharvadze.geosms.MyNotificationManager;
import com.ioane.sharvadze.geosms.conversationsList.ConversationsListUpdater;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;
import com.ioane.sharvadze.geosms.objects.SMS;

import utils.Constants;

/**
 * Class SmsDispatcher that receives and handles
 * various sms broadcasts.
 *
 * Some of them are :
 *  * SMS_SENT
 *  * SMS_RECEIVED and SMS_DELIVER
 *  * SMS_FAILED
 *
 * Created by Ioane on 3/1/2015.
 */
public class SmsDispatcher extends BroadcastReceiver {

    private static final String TAG = SmsDispatcher.class.getSimpleName();

    private static final int VIBRATE_LENGTH = 100;


    /**
     * No thread is shown to user.
     */
    public static final int THREAD_ID_NONE  = -123123;

    /**
     * Saves current shown  threadId that user interacts with.
     */
    private static long currentThreadId = THREAD_ID_NONE;

    public static void updateThreadId(long threadId){
        currentThreadId = threadId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG,"onReceive() :" + action);


        if(action == null) return;
        // New versions only listen to SMS_DELIVER.
        if(Build.VERSION.SDK_INT >= 19 && action.contains(Constants.Actions.SMS_RECEIVED)){
            Log.i(TAG,"action dismissed " +action);
            return;
        }


        if(action.equals( Constants.Actions.MESSAGE_SENT)){
            handleSmsSend(context, intent);

        }else if(action.contains(Constants.Actions.SMS_DELIVER) ||
                action.contains(Constants.Actions.SMS_RECEIVED)){
            handleSmsReceive(context, intent);
        }else{
            Log.w(TAG,"unknown action "+action);

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSubText(action);
            mNotificationManager.notify(1212112, builder.build());
        }

    }

    private void handleSmsReceive(Context ctx,Intent intent){
        Bundle bundle = intent.getExtras();
        if(bundle == null){
            Log.w(TAG,"bundle is empty");
            return;
        }
        Log.i(TAG,"sms received");

        ContentValues values = SMS.getContentValuesFromBundle(bundle);
        if(values == null)
            return;
        Uri smsUri = ctx.getContentResolver().insert(Uri.parse("content://sms/"), values);

        String address = values.getAsString(Constants.ADDRESS);
        Contact contact = new Contact(ctx,address);
        long threadId = Conversation.getOrCreateThreadId(ctx, contact.getAddress());

        SMS sms = new SMS(values);
        // If user isn't on this conversation, we notify
        if(threadId !=  currentThreadId || threadId == THREAD_ID_NONE){
            NotificationManager mNotificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
            boolean isSummary =  MyNotificationManager.buildSmsReceiveNotification(ctx, contact, sms, builder);

            int notif_id = isSummary ? MyNotificationManager.ID_SMS_RECEIVED : (int)threadId;
            mNotificationManager.notify(notif_id,builder.build());
        }else {
            // if user is on this chat , make no notification , just slight vibration
            values = new ContentValues();
            values.put(Constants.MESSAGE.READ, 1); // is read
            // update message as read.
            ctx.getContentResolver().update(smsUri, values,null,null);
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(VIBRATE_LENGTH);
        }
    }


    private void handleSmsSend(Context ctx, final Intent intent){
        Uri pendingSmsUri = intent.getData();
        if(pendingSmsUri == null){
            Log.w(TAG,"pendingSmsURI is null");
            return;
        }
        Log.i(TAG,pendingSmsUri.toString());

        ContentValues values = new ContentValues();

        long threadId = intent.getLongExtra(Constants.RECIPIENT_IDS,0);
        if(threadId != 0)
            ConversationsListUpdater.updateConversation(threadId);

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
            Log.w(TAG,"couldn't update sms! some bug here");
        }
    }

}

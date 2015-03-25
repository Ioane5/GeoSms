package broadcastReceivers;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import utils.Constants;
import com.ioane.sharvadze.geosms.MyNotificationManager;
import utils.Utils;
import com.ioane.sharvadze.geosms.conversationsList.ConversationsListUpdater;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.SMS;

/**
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
    private static int currentThreadId = THREAD_ID_NONE;

    public static void updateThreadId(int threadId){
        currentThreadId = threadId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG,"onReceive() :" + action);
        if(action == null) return;

        switch (action){
            case Constants.Actions.MESSAGE_SENT:
                handleSmsSend(context,intent);
                break;

            case Constants.Actions.MESSAGE_DELIVERED_1:
            case Constants.Actions.MESSAGE_DELIVERED_2:
                handleSmsReceive(context,intent);
                break;

            default:
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
                Uri smsUri = ctx.getContentResolver().insert(Uri.parse("content://sms/"), values);

                String address = values.getAsString(Constants.ADDRESS);
                Contact contact = new Contact(ctx,address);
                int threadId = Utils.getSmsThreadId(ctx, smsUri);
                contact.setThreadId(threadId);

                // TODO delete this ConversationsListUpdater.updateConversation(threadId);
                SMS sms = new SMS(values);
                Log.i(TAG,"current thread_id = " + currentThreadId  + " contact.ThreadId = " + contact.getThreadId());
                if(contact.getThreadId() !=  currentThreadId){
                    NotificationManager mNotificationManager =
                            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                    // mId allows you to update the notification later on.
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
                    boolean isSummary =  MyNotificationManager.buildSmsReceiveNotification(ctx, contact, sms, builder);
                    //mNotificationManager.notify(MyNotificationManager.ID_SMS_RECEIVED,notif);
                    int notif_id = isSummary ? MyNotificationManager.ID_SMS_RECEIVED : contact.getThreadId();
                    mNotificationManager.notify(notif_id,builder.build());
                }else {
                    values = new ContentValues();
                    values.put(Constants.MESSAGE.READ, 1); // is read
                    // update message as read.
                    ctx.getContentResolver().update(smsUri, values,null,null);
                    // if user is on this chat , make no notification , just slight vibration
                    Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(VIBRATE_LENGTH);
                }

                return null;
            }
        }.execute(ctx,intent.getExtras());
    }


    private void handleSmsSend(Context ctx, final Intent intent){
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

                int threadId = intent.getIntExtra(Constants.RECIPIENT_ID,0);
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
                    // ignore exception
                }
                return null;
            }
        }.execute(ctx,pendingSmsUri);
    }

}
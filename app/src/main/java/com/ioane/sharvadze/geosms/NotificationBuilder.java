package com.ioane.sharvadze.geosms;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.ioane.sharvadze.geosms.conversation.ConversationActivity;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.SMS;

/**
 * Created by Ioane on 3/4/2015.
 */
public class NotificationBuilder {

    public static Notification buildSmsReceiveNotification(Context ctx,Contact contact,SMS sms){

        Bitmap photo;
        if(contact.getPhotoUri() == null){
            photo =  BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.no_image);
        }else {
            photo = Utils.getPhotoFromURI(contact.getPhotoUri(),ctx,100);
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setLargeIcon(photo)
                        .setSmallIcon(R.mipmap.geosms_icon)
                        .setContentTitle(contact.getName() == null? contact.getAddress():contact.getName())
                        .setContentText(sms.getText())
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setCategory(Notification.CATEGORY_MESSAGE);




        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(ctx, ConversationActivity.class);
        resultIntent.putExtra(Constants.CONTACT_BUNDLE,contact.getBundle());

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ConversationActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        resultIntent.putExtra(Constants.CONTACT_BUNDLE,contact.getBundle());
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setOnlyAlertOnce(true); // TODO check what is this...
        mBuilder.setAutoCancel(true);
        Notification notification = mBuilder.build();
        // TODO check if this is better approach
        return notification;
    }
}

package com.ioane.sharvadze.geosms.conversation;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.SMS;

import utils.Utils;

/**
 * Class ConversationCursorAdapter
 * (custom cursor adapter)
 *
 * Created by Ioane on 2/26/2015.
 */
public class ConversationCursorAdapter extends CursorAdapter {


    private static final  String TAG = ConversationCursorAdapter.class.getSimpleName();

    private Contact contact;

    private Bitmap MY_IMAGE;

    private int receivedMsgCol;

    public ConversationCursorAdapter(Context context, Cursor c, int flags,Contact contact) {
        super(context, c, flags);
        this.contact = contact;
        MY_IMAGE = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_no_image);
        // make it circle like.
        MY_IMAGE = Utils.getCircleBitmap(MY_IMAGE);
        this.receivedMsgCol = context.getResources().getColor(R.color.themeLight);
    }

    public ConversationCursorAdapter(Context context, Cursor c, boolean autoRequery ,Contact contact) {
        super(context, c, autoRequery);
        this.contact = contact;
        MY_IMAGE = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_no_image);
        // make it circle like.
        MY_IMAGE = Utils.getCircleBitmap(MY_IMAGE);
        this.receivedMsgCol = context.getResources().getColor(R.color.themeLight);
    }


    private class ViewHolder {
        TextView messageView;
        TextView nameView;
        TextView deliveryStatusView;
        TextView timeView;
        ImageView photo;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.message_item,parent,false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SMS message = new SMS(cursor);

        ViewHolder holder = (ViewHolder)view.getTag();

        if (holder == null) {
            holder = new ViewHolder();
            holder.nameView = (TextView)view.findViewById(R.id.name_view);
            holder.messageView = (TextView)view.findViewById(R.id.message_text_view);
            holder.deliveryStatusView = (TextView)view.findViewById(R.id.delivery_status_view);
            holder.timeView = (TextView)view.findViewById(R.id.time_view);
            holder.photo = (ImageView)view.findViewById(R.id.message_contact_photo);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }

        SMS.MsgType type = message.getMsgType();

        SMS nextSms = null;
        if(cursor.moveToPrevious()){
            nextSms = new SMS(cursor);
        }

        holder.nameView.setText(null);
        holder.photo.setVisibility(View.INVISIBLE);

        if (holder.deliveryStatusView.length() > 0) {
            holder.deliveryStatusView.setText("");
        }
        /*
            if this sms is first , or is received from contact
            let's show header. like photo and name...
         */
        if(type != SMS.MsgType.RECEIVED && (nextSms == null || nextSms.getMsgType() == SMS.MsgType.RECEIVED)){
            holder.nameView.setText(R.string.me);
            holder.photo.setImageBitmap(MY_IMAGE);
            holder.photo.setVisibility(View.VISIBLE);
        }

        view.setBackgroundColor(Color.TRANSPARENT);

        switch (type){
            case SENT:
                break;
            case PENDING:
                holder.deliveryStatusView.setText(R.string.sms_pending);
                holder.deliveryStatusView.setTextColor(Color.GRAY);
                break;
            case DRAFT:
                holder.deliveryStatusView.setText(R.string.sms_draft);
                holder.deliveryStatusView.setTextColor(Color.GRAY);
                break;
            case FAILED:
                holder.deliveryStatusView.setText(R.string.sms_failed);
                holder.deliveryStatusView.setTextColor(Color.RED);
                break;
            case RECEIVED:
                view.setBackgroundColor(receivedMsgCol);
                /*
                    If this is sms from sender or is first, let's show header...
                 */
                if(nextSms == null || nextSms.getMsgType() != SMS.MsgType.RECEIVED){
                    holder.nameView.setText(TextUtils.isEmpty(contact.getName())? contact.getAddress():contact.getName());
                    holder.photo.setImageBitmap(contact.getPhoto());
                    holder.photo.setVisibility(View.VISIBLE);

                    break;
                }

        }

        holder.messageView.setText(message.getText());
        CharSequence formattedTime = DateUtils.getRelativeTimeSpanString(message.getDate().getTime(),
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        holder.timeView.setText(formattedTime);

    }
}

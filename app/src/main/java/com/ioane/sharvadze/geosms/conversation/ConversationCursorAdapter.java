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
import com.ioane.sharvadze.geosms.Utils;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.SMS;

/**
 * Created by Ioane on 2/26/2015.
 */
public class ConversationCursorAdapter extends CursorAdapter {


    private static final  String TAG = ConversationCursorAdapter.class.getSimpleName();

    private Contact contact;

    private Bitmap MY_IMAGE;

    public ConversationCursorAdapter(Context context, Cursor c, int flags,Contact contact) {
        super(context, c, flags);
        this.contact = contact;
        MY_IMAGE = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.no_image);
        // make it circle like.
        MY_IMAGE = Utils.getCircleBitmap(MY_IMAGE);
    }

    public ConversationCursorAdapter(Context context, Cursor c, boolean autoRequery,Contact contact) {
        super(context, c, autoRequery);
        this.contact = contact;
        MY_IMAGE = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.no_image);
        // make it circle like.
        MY_IMAGE = Utils.getCircleBitmap(MY_IMAGE);
    }


    static class ViewHolder {
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

        holder.nameView.setText("me");
        holder.photo.setImageBitmap(MY_IMAGE);
        if (holder.deliveryStatusView.length() > 0) {
            holder.deliveryStatusView.setText("");
        }

        switch (type){
            case SENT:
                view.setBackgroundColor(Color.WHITE);
                break;
            case PENDING:
                view.setBackgroundColor(Color.BLUE);
                holder.deliveryStatusView.setText("pending");
                break;
            case DRAFT:
                view.setBackgroundColor(Color.GRAY);
                holder.deliveryStatusView.setText("draft");
                break;
            case FAILED:
                view.setBackgroundColor(Color.RED);
                holder.deliveryStatusView.setText("failed");
                break;
            case RECEIVED:
                view.setBackgroundColor(Color.parseColor("#1F44CC0A"));
                holder.nameView.setText(TextUtils.isEmpty(contact.getName())? contact.getAddress():contact.getName());
                holder.photo.setImageBitmap(contact.getPhoto());
                break;
        }

        holder.messageView.setText(message.getText());
        CharSequence formattedTime = DateUtils.getRelativeTimeSpanString(message.getDate().getTime(),
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        holder.timeView.setText(formattedTime);
    }
}

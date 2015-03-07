package com.ioane.sharvadze.geosms.conversationsList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.Utils;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;

import java.util.List;

/**
 * Created by Ioane on 2/23/2015.
 */
public class ConversationsListAdapter extends ArrayAdapter<Conversation> {


    private final String TAG = ConversationsListAdapter.class.getSimpleName();


    //private static Bitmap DEFAULT_IMAGE;

    private class ViewHolder {
        TextView contactNameView;
        TextView messageView;
        TextView messageDateView;
        ImageView contactImageView;
    }

    public ConversationsListAdapter(Context context, int resource, List<Conversation> objects) {
        super(context, resource, objects);

//        DEFAULT_IMAGE = BitmapFactory.decodeResource(context.getResources(),
//                R.mipmap.no_image);
//        // make it circle like.
//        DEFAULT_IMAGE = Utils.getCircleBitmap(DEFAULT_IMAGE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.conversation_item, null);

            holder = new ViewHolder();
            holder.contactNameView = (TextView)view.findViewById(R.id.contact_name_text_view);
            holder.messageDateView = (TextView)view.findViewById(R.id.last_message_date_view);
            holder.messageView = (TextView)view.findViewById(R.id.last_message_text_view);
            holder.contactImageView = (ImageView)view.findViewById(R.id.contact_picture_image_view);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }
        Conversation conversation = getItem(position);
        if(conversation == null) return view;


        // not to show different values if contact is null...
        holder.contactImageView.setImageBitmap(null);
        holder.messageView.setText("");

        Contact contact = conversation.getContact();
        holder.contactImageView.setTag(contact);

        if(contact != null){
            if(contact.getName() != null && !contact.getName().equals("")){
                holder.contactNameView.setText(contact.getName());
            }else{
                holder.contactNameView.setText(contact.getAddress());
            }
            if(contact.getPhotoUri() != null){
                holder.contactImageView.setImageBitmap(contact.getPhoto());
            }
        }
        holder.messageView.setText(conversation.getLastMessage());

        if(conversation.getDate() != null){
            CharSequence formattedTime = DateUtils.getRelativeTimeSpanString(conversation.getDate().getTime(),System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
            holder.messageDateView.setText(formattedTime);
        }else{
            holder.messageDateView.setText("");
        }

        if(!conversation.isMessageRead()){
            holder.messageView.setTypeface(null, Typeface.BOLD_ITALIC);
        }else{
            holder.messageView.setTypeface(null,Typeface.NORMAL);
        }

        return view;
    }
}
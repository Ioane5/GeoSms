package com.ioane.sharvadze.geosms.conversationsList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.ioane.sharvadze.geosms.Constants;
import com.ioane.sharvadze.geosms.conversation.ConversationActivity;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;

/**
 * Created by Ioane on 2/20/2015.
 */
public class ConversationItemClickListener implements  AdapterView.OnItemClickListener{

    private ArrayAdapter<Conversation> adapter;
    private Context context;

    public ConversationItemClickListener(ArrayAdapter<Conversation> adapter, Context context){
        this.adapter = adapter;
        this.context = context;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation conversation = adapter.getItem(position);
        Contact contact = conversation.getContact();

        Intent i = new Intent(context,ConversationActivity.class);
        i.putExtra(Constants.CONTACT_BUNDLE,contact != null? contact.getBundle() : null);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
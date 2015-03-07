package com.ioane.sharvadze.geosms.conversationsList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ioane.sharvadze.geosms.MyActivity;
import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.Utils;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;

import java.util.ArrayList;

public class ConversationsListActivity extends MyActivity{

    private final String TAG  = ConversationsListActivity.class.getSimpleName();

    private ListView conversationList;

    private Cursor loadingData;

    private ArrayAdapter<Conversation> listAdapter;

    /**
     * This means how many conversations to fetch at first time.
     * And than we will fetch by thread in parallel to not make
     * user wait.
     */
    private final int INITIAL_CONVERSATION_LOAD_NUM = 8;


    /**
     * This int is just to
     */
    private final int DEFAULT_SMS_REQUEST = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.main_title);
        setContentView(R.layout.activity_conversations_list);

        listAdapter = new ConversationsListAdapter(getBaseContext(),
                R.layout.conversation_item,new ArrayList<Conversation>());

        // fetch before we show...
        Log.i(TAG,"Fetching data...");
        fetchConversations();

        conversationList = (ListView) findViewById(R.id.conversations_list_view);
        conversationList.setAdapter(listAdapter);
        defaultAppResolve();
        // Listen to clicks
        conversationList.setOnItemClickListener(new ConversationItemClickListener(listAdapter, getBaseContext()));
    }



    public void onContactImageClick(View v){
        Log.i(TAG,"contact clicked " + v.getClass().getSimpleName());

        Contact contact = (Contact)v.getTag();
        if(contact == null){
            Log.i(TAG,"contact null");
            return;
        }else {
            Intent intent = null;
            if(TextUtils.isEmpty(contact.getName())){
                intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,ContactsContract.Contacts.CONTENT_URI);
                intent.setData(Uri.fromParts("tel", contact.getAddress(), null));
                intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.getName());
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, contact.getAddress());
                intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            }else {
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contact.getId()));
                intent = new Intent(Intent.ACTION_VIEW, uri);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(intent);
        }
    }

    private void defaultAppResolve(){

        if (!Utils.isDefaultSmsApp(getBaseContext())) {
            Log.d(TAG,"not default app");
            // App is not default.
            // Show the "not currently set as the default SMS app" interface
            View viewGroup = findViewById(R.id.not_default_app);
            viewGroup.setVisibility(View.VISIBLE);

            // Set up a button that allows the user to change the default SMS app
            Button button = (Button) findViewById(R.id.change_default_app);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG,"clicked on button");
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,getPackageName());
                    startActivityForResult(intent, DEFAULT_SMS_REQUEST);
                }
            });
        } else {
            // App is the default.
            // Hide the "not currently set as the default SMS app" interface
            View viewGroup = findViewById(R.id.not_default_app);
            viewGroup.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case DEFAULT_SMS_REQUEST:
                int msgId;
                if(resultCode == Activity.RESULT_OK){
                    View viewGroup = findViewById(R.id.not_default_app);
                    viewGroup.setVisibility(View.GONE);
                    msgId = R.string.sms_default_success;
                }else{
                    msgId = R.string.sms_default_fail;
                }
                Toast.makeText(getBaseContext(),msgId,Toast.LENGTH_SHORT).show();

        }
    }

    private void fetchConversations() {
        /* We save data in static field.  So if activity is destroid we
         * won't need to re-fetch the data , because it was saved in field.
         */
//      if(isFetched) return;
        SmartConversationFetcher task = new SmartConversationFetcher(getBaseContext(),INITIAL_CONVERSATION_LOAD_NUM,listAdapter);
        task.execute();
//      isFetched = true; // now it's fetched.
    }



}

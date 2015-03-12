package com.ioane.sharvadze.geosms.conversationsList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ioane.sharvadze.geosms.Constants;
import com.ioane.sharvadze.geosms.MyActivity;
import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.Utils;
import com.ioane.sharvadze.geosms.conversation.ConversationActivity;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;

import java.util.ArrayList;
import java.util.List;

public class ConversationsListActivity extends MyActivity implements AdapterView.OnItemClickListener{

    private final String TAG  = ConversationsListActivity.class.getSimpleName();

    private ListView conversationList;

    private Cursor loadingData;

    private ArrayAdapter<Conversation> listAdapter;

    private static boolean onResume = false;


    /**
     * This means how many conversations to fetch at first time.
     * And than we will fetch by thread in parallel to not make
     * user wait.
     */
    private final int INITIAL_CONVERSATION_LOAD_NUM = 8;

    private static List<Integer> updateConversations =  new ArrayList<Integer>();


    /**
     * This int is just to
     */
    private final int DEFAULT_SMS_REQUEST = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.main_title);
        setContentView(R.layout.activity_conversations_list);
        // if it's default app , it changes layout.
        defaultAppResolve();

        listAdapter = new ConversationsListAdapter(getBaseContext(),
                R.layout.conversation_item,new ArrayList<Conversation>());

        // fetch before we show...
        Log.i(TAG,"Fetching data...");
        fetchConversations();
        new ConversationsListUpdater(this,listAdapter);
        conversationList = (ListView) findViewById(R.id.conversations_list_view);
        conversationList.setAdapter(listAdapter);
        // Listen to clicks
        conversationList.setOnItemClickListener(this);
        // listen for conversation updates
        initMultiChoiceListView(conversationList);
    }


    private void initMultiChoiceListView(ListView listView){
        new ConversationsListCAB(getApplicationContext(),listView,listAdapter,
                new AlertDialog.Builder(ConversationsListActivity.this));
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

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
            }else {
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contact.getId()));
                ContactsContract.QuickContact.showQuickContact(this,(View)findViewById(R.id.quick_contact),uri,
                        ContactsContract.QuickContact.MODE_MEDIUM,null);
            }
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
        SmartConversationFetcher task = new SmartConversationFetcher(getBaseContext(),INITIAL_CONVERSATION_LOAD_NUM,listAdapter);
        task.execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG,"bla bla");
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            overridePendingTransition(R.animator.abc_slide_in_left, R.anim.abc_fade_out);
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation conversation = listAdapter.getItem(position);
        Contact contact = conversation.getContact();

        Intent i = new Intent(ConversationsListActivity.this,ConversationActivity.class);
        i.putExtra(Constants.CONTACT_BUNDLE,contact != null? contact.getBundle() : null);

        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        //overridePendingTransition(R.animator.abc_slide_in_left, R.anim.abc_fade_out);
    }
}

package com.ioane.sharvadze.geosms.conversationsList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ioane.sharvadze.geosms.MyNotificationManager;
import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.conversation.ConversationActivity;
import com.ioane.sharvadze.geosms.conversationsListFatchers.ConversationLoader;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import newConversation.NewConversationActivity;
import utils.Constants;
import utils.MyActivity;
import utils.Utils;

public class ConversationsListActivity extends MyActivity implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<Conversation>> {

    private final String TAG = ConversationsListActivity.class.getSimpleName();

    private ListView conversationList;

    private Cursor loadingData;

    private ArrayAdapter<Conversation> listAdapter;


    /**
     * This means how many conversations to fetch at first time.
     * And than we will fetch by thread in parallel to not make
     * user wait.
     */
    private final int INITIAL_CONVERSATION_LOAD_NUM = 8;

    private static List<Integer> updateConversations = new ArrayList<Integer>();


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

        // load list from cache
        ArrayList<Conversation> list = Utils.loadContactList(getBaseContext());
        if(list == null) // or if we couldn't read cache , create new...
            list = new ArrayList<>();
        listAdapter = new ConversationsListAdapter(getBaseContext(),
                R.layout.conversation_item, list);

        conversationList = (ListView) findViewById(R.id.conversations_list_view);
        conversationList.setAdapter(listAdapter);
        // Listen to clicks
        conversationList.setOnItemClickListener(this);
        // listen for conversation updates
        initMultiChoiceListView(conversationList);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(conversationList);

        MyNotificationManager.clearNotifications(getBaseContext());

        getLoaderManager().initLoader(0, null, this);
    }


    private void defaultAppResolve() {

        if (!Utils.isDefaultSmsApp(getBaseContext())) {
            Log.d(TAG, "not default app");
            // App is not default.
            // Show the "not currently set as the default SMS app" interface
            View viewGroup = findViewById(R.id.not_default_app);
            viewGroup.setVisibility(View.VISIBLE);

            // Set up a button that allows the user to change the default SMS app
            setUpDefaultAppResolver();
        } else {
            // App is the default.
            // Hide the "not currently set as the default SMS app" interface
            View viewGroup = findViewById(R.id.not_default_app);
            viewGroup.setVisibility(View.GONE);
        }
    }

    @TargetApi(19)
    private void setUpDefaultAppResolver(){
        Button button = (Button) findViewById(R.id.change_default_app);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "clicked on button");
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, DEFAULT_SMS_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DEFAULT_SMS_REQUEST:
                int msgId;
                if (resultCode == Activity.RESULT_OK) {
                    View viewGroup = findViewById(R.id.not_default_app);
                    viewGroup.setVisibility(View.GONE);
                    msgId = R.string.sms_default_success;
                } else {
                    msgId = R.string.sms_default_fail;
                }
                Toast.makeText(getBaseContext(), msgId, Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public Loader<ArrayList<Conversation>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ConversationLoader(getBaseContext());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Conversation>> loader, ArrayList<Conversation> data) {
        // Set the new data in the adapter.
        listAdapter.clear();
        listAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Conversation>> loader) {
        // Clear the data in the adapter.
        listAdapter.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation conversation = listAdapter.getItem(position);
        Contact contact = conversation.getContact();

        Intent i = new Intent(ConversationsListActivity.this, ConversationActivity.class);
        // TODO pass arrayList...
        i.putExtra(Constants.CONTACT_BUNDLE, contact != null ? contact.getBundle() : null);

        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        //overridePendingTransition(R.animator.abc_slide_in_left, R.anim.abc_fade_out);
    }

    /**
     * When user clicks new_conversation button we redirect
     * him to new conversation activity.
     *
     * @param view Button that was clicked.
     */
    public void newConversation(View view){
        Intent i = new Intent(ConversationsListActivity.this, NewConversationActivity.class);
        startActivity(i);
    }

    private void initMultiChoiceListView(ListView listView) {
        new ConversationsListCAB(getApplicationContext(), listView, listAdapter,
                new AlertDialog.Builder(ConversationsListActivity.this));
    }

    public void onContactImageClick(View v) {
        Log.i(TAG, "contact clicked " + v.getClass().getSimpleName());

        Contact contact = (Contact) v.getTag();
        if (contact == null) {
            Log.i(TAG, "contact null");
        } else {
            Intent intent;
            if (TextUtils.isEmpty(contact.getName())) {
                intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, ContactsContract.Contacts.CONTENT_URI);
                intent.setData(Uri.fromParts("tel", contact.getAddress(), null));
                intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.getName());
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, contact.getAddress());
                intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
            } else {
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contact.getId()));
                ContactsContract.QuickContact.showQuickContact(this, findViewById(R.id.quick_contact), uri,
                        ContactsContract.QuickContact.MODE_MEDIUM, null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation_list, menu);
        return true;
    }

}

package com.ioane.sharvadze.geosms.conversationsList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ioane.sharvadze.geosms.MyNotificationManager;
import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.conversation.ConversationActivity;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.ArrayList;
import java.util.List;

import newConversation.NewConversationActivity;
import utils.Constants;
import utils.MyActivity;
import utils.Utils;

public class ConversationsListActivity extends MyActivity implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<Conversation>> {

    private final String TAG = ConversationsListActivity.class.getSimpleName();

    private ArrayAdapter<Conversation> listAdapter;

    private ListView mListView;

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

        ArrayList<Conversation> conversations = Utils.loadConversationsFromCache(getBaseContext());
        if(conversations == null) conversations = new ArrayList<>();

        listAdapter = new ConversationsListAdapter(getBaseContext(),
                R.layout.conversation_item, conversations);

        mListView = (ListView) findViewById(R.id.conversations_list_view);
        mListView.setAdapter(listAdapter);
        // Listen to clicks
        mListView.setOnItemClickListener(this);
        // listen for conversation updates
        initMultiChoiceListView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(mListView);

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
        return new ConversationListLoader(getBaseContext());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Conversation>> loader, ArrayList<Conversation> data) {
        // Set the new data in the adapter.
        listAdapter.clear();
        listAdapter.addAll(data);
        listAdapter.notifyDataSetChanged();
        Utils.cacheConversations(getBaseContext(), data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Conversation>> loader) {
        // Clear the data in the adapter.
        listAdapter.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation conversation = listAdapter.getItem(position);

        Intent i = new Intent(ConversationsListActivity.this, ConversationActivity.class);

        i.putExtra(Constants.CONTACT_DATA, conversation.getContacts());
        i.putExtra(Constants.THREAD_ID, conversation.getId());

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

    private void initMultiChoiceListView() {
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final String text = getResources().getString(R.string.selected);
                mode.setTitle(String.format("%s %d" , text, mListView.getCheckedItemCount()));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.conversations_list_cab, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationsListActivity.this);

                        SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
                        final List<Integer> checkedIds = new ArrayList<>(checkedItemPositions.size());

                        for (int i = 0; i < checkedItemPositions.size(); i++)
                            if (checkedItemPositions.get(checkedItemPositions.keyAt(i)))
                                checkedIds.add(checkedItemPositions.keyAt(i));


                        builder.setTitle(R.string.warning);
                        builder.setMessage(checkedIds.size() == 1 ? R.string.delete_warning_msg_single :
                                R.string.delete_warning_msg_plural);
                        builder.setPositiveButton("YES", new DatePickerDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, final int which) {
                                ArrayList<Conversation> deleteList = new ArrayList<>(checkedIds.size());
                                for (long id : checkedIds) {
                                    Conversation toDelete = listAdapter.getItem((int) id);
                                    deleteList.add(toDelete);
                                    listAdapter.remove(toDelete);
                                }
                                listAdapter.notifyDataSetChanged();
                                new AsyncUndoDelete(deleteList, Constants.UNDO_TIME, checkedIds).execute();
                                mode.finish();
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mode.finish();
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    public void onContactImageClick(View v) {
        Contact contact = (Contact) v.getTag(R.string.contact);
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

                // TODO need any flags?
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
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

    private class AsyncUndoDelete extends AsyncTask<Void,Void,Boolean>{

        ArrayList<Conversation> mToDeleteList;
        int mSecondsToWait;
        List<Integer> mIndexes;

        /**
         * @param toDeleteList list to delete if not undo.
         * @param secondsToWait how many seconds to wait.
         */
        public AsyncUndoDelete(ArrayList<Conversation> toDeleteList,int secondsToWait,List<Integer> indexes){
            mToDeleteList = toDeleteList;
            mSecondsToWait = secondsToWait;
            mIndexes = indexes;
        }

        private void restore(){
            for(int i=0;i<mIndexes.size();i++){
                listAdapter.insert(mToDeleteList.get(i),mIndexes.get(i));
            }
            listAdapter.notifyDataSetChanged();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String text = getResources().getString(R.string.conversations_deleted);

            SnackbarManager.show(
                    Snackbar.with(getApplicationContext())
                        .text(String.format("%d %s", mToDeleteList.size(), text))
                        .actionLabel(R.string.undo).actionColorResource(R.color.themePrimary)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                cancel(false);
                                restore();
                            }
                        })
                    ,ConversationsListActivity.this );
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Thread.sleep(mSecondsToWait * 1000);
                if(isCancelled())
                    return false;
            } catch (InterruptedException e) {
                return false;
            }
            for(Conversation toDelete: mToDeleteList){
                getContentResolver().delete(
                        Uri.parse("content://mms-sms/conversations"),
                        "thread_id=?", new String[]{Long.toString(toDelete.getId())});
            }
            return true;
        }


    }

}

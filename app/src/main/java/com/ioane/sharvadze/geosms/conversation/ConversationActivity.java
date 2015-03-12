package com.ioane.sharvadze.geosms.conversation;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.ioane.sharvadze.geosms.Constants;
import com.ioane.sharvadze.geosms.GeoSmsManager;
import com.ioane.sharvadze.geosms.MyActivity;
import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.Utils;
import com.ioane.sharvadze.geosms.conversationsList.ConversationsListUpdater;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.SMS;

import java.util.Date;


public class ConversationActivity extends MyActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = ConversationActivity.class.getSimpleName();

    private ConversationCursorAdapter adapter;

    private ToggleButton webUseToggle;

    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        webUseToggle = (ToggleButton)findViewById(R.id.use_web_toggle_button);
        ImageButton button = (ImageButton)findViewById(R.id.send_button);

        contact = getContact();
        button.setOnClickListener(new SendButtonListener(contact));

        setTitle(TextUtils.isEmpty(contact.getName())? contact.getAddress(): contact.getName());

        adapter = new ConversationCursorAdapter(getBaseContext(),null,true,contact);
        ListView listView = (ListView)findViewById(R.id.conversation_list_view);
        listView.setAdapter(adapter);

        EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
        if(!Utils.isDefaultSmsApp(this)){
            editText.setFocusable(false);
            editText.setHint(R.string.set_default_app_to_send);
        }else {
            SharedPreferences drafts = getSharedPreferences(Constants.DRAFTS_FILE,MODE_PRIVATE);
            String text = drafts.getString(contact.getAddress(),null);
            if(!TextUtils.isEmpty(text))
                editText.setText(text);
        }

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.RECIPIENT_ID, contact.getThreadId());
        markConversationAsRead(contact);
        getLoaderManager().initLoader(0, bundle, this);
    }


    private Contact getContact(){
        Bundle extras = getIntent().getExtras();
        Bundle contactData = (Bundle)extras.getBundle(Constants.CONTACT_BUNDLE);
        Contact contact = null;
        if(contactData == null){
            Log.w(TAG,"contactData is not provided");
            return null;
        }
        return new Contact(contactData);
    }


    private void markConversationAsRead(final Contact contact){
        int threadId = contact.getThreadId();
        new AsyncTask<Integer,Void,Void>(){

            @Override
            protected Void doInBackground(Integer... params) {
                int threadId = params[0];
                ContentValues cv =  new ContentValues();
                cv.put(Constants.MESSAGE.READ,1);
                ConversationsListUpdater.markAsRead(threadId);
                getContentResolver().update(Uri.parse("content://sms"),cv , ("thread_id = " + threadId), null);
                return null;
            }
        }.execute(threadId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
        if(!editText.isFocusable()) return; // not save when it's not default
        SharedPreferences drafts = getSharedPreferences(Constants.DRAFTS_FILE,MODE_PRIVATE);
        SharedPreferences.Editor editor = drafts.edit();

        if(editText.getText() == null || editText.getText().toString().equals("")){
            editor.remove(contact.getAddress());
        }else {
            Integer i = contact.getId();
            editor.putString(contact.getAddress(), editText.getText().toString());
        }
        editor.apply();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int thread_id = args.getInt(Constants.RECIPIENT_ID);
        Uri uri = Uri.parse("content://sms/");
        return new CursorLoader(getBaseContext(),uri,null,"thread_id = ?" ,
                new String[]{Integer.toString(thread_id)},"date asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    private boolean isSendWeb(){
        return webUseToggle.isChecked();
    }



    /**
     * Class for managing clicked send button.
     */
    private class SendButtonListener implements View.OnClickListener{

        private Contact contact;
        private GeoSmsManager smsManager;

        public SendButtonListener(Contact contact){
            this.contact = contact;
            smsManager = new GeoSmsManager((ListView)findViewById(R.id.conversation_list_view),
                    getBaseContext(),contact);
        }
        @Override
        public void onClick(View view) {
            EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
            if(TextUtils.isEmpty(editText.getText())) return;

            String message = editText.getText().toString();
            // it's a draft
            if(contact == null){
                SMS sms = new SMS(message,new Date(System.currentTimeMillis()), SMS.MsgType.DRAFT,true,false,null);
                smsManager.saveDraft(sms);
            }else{
                SMS sms = new SMS(message,new Date(System.currentTimeMillis()), SMS.MsgType.PENDING,true,false,null);
                smsManager.sendSms(sms,contact.getAddress(),isSendWeb());
            }
            editText.setText("");
        }
    }
}

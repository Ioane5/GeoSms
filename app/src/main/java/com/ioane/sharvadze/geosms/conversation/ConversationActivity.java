package com.ioane.sharvadze.geosms.conversation;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ioane.sharvadze.geosms.GeoSmsManager;
import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.conversationsList.ConversationsListUpdater;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.SMS;

import java.util.Date;

import broadcastReceivers.SmsDispatcher;
import utils.Constants;
import utils.MyActivity;
import utils.Utils;


public class ConversationActivity extends MyActivity implements LoaderManager.LoaderCallbacks<Cursor> ,TextWatcher{


    private static final String TAG = ConversationActivity.class.getSimpleName();

    private ConversationCursorAdapter adapter;

    private ToggleButton webUseToggle;

    private ImageButton button;

    private TextView symbolCounter;

    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        webUseToggle = (ToggleButton)findViewById(R.id.use_web_toggle_button);
        button = (ImageButton)findViewById(R.id.send_button);
        button.setEnabled(false);

        EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
        symbolCounter = (TextView)findViewById(R.id.symbol_counter);
        editText.addTextChangedListener(this);

        contact = getContact();
        if(contact == null){
            editText.setEnabled(false);
            editText.setFocusable(false);
            return;
        }
        button.setOnClickListener(new SendButtonListener(contact));

        setTitle(TextUtils.isEmpty(contact.getName())? contact.getAddress(): contact.getName());

        adapter = new ConversationCursorAdapter(getBaseContext(),null,true,contact);
        ListView listView = (ListView)findViewById(R.id.conversation_list_view);
        listView.setAdapter(adapter);

        if(!Utils.isDefaultSmsApp(this)){
            editText.setFocusable(false);
            editText.setHint(R.string.set_default_app_to_send);
            webUseToggle.setEnabled(false);
            button.setEnabled(false);
        }else {
            SharedPreferences prefs = getSharedPreferences(Constants.DRAFTS_FILE,MODE_PRIVATE);
            String text = prefs.getString(contact.getAddress(),null);
            if(!TextUtils.isEmpty(text)) {
                button.setEnabled(true);
                editText.setText(text);
            }
            boolean isChecked = prefs.getBoolean(Constants.TOGGLE_CHECKED,false);
            webUseToggle.setChecked(isChecked);
        }
        startLoader();
    }

    /**
     * This method inits loader when thread_id is resolved.
     */
    private void startLoader(){
        // if we needn't thread_id resolve we don't create new thread.
        if(contact.getThreadId() != 0){
            Bundle bundle = new Bundle();
            bundle.putInt(Contact.THREAD_ID,contact.getThreadId());
            getLoaderManager().initLoader(0, bundle, this);
            return;
        }
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                // this part resolves thread id.
                int thread_id = contact.getThreadId();
                String address = contact.getAddress();
                ContentValues val = new ContentValues();
                val.put(Constants.ADDRESS,address);
                val.put(Constants.MESSAGE.READ, 1);
                // insert some message
                Uri insertedSmsURI = getContentResolver().insert(Uri.parse("content://sms/"),val );
                Cursor c = getContentResolver().query(insertedSmsURI,
                        new String[]{Constants.THREAD_ID},null,null,null);
                if(c!= null){
                    if(c.moveToFirst()){
                        thread_id = c.getInt(c.getColumnIndex(Constants.THREAD_ID));
                        contact.setThreadId(thread_id);
                    }
                    c.close();
                }
                getContentResolver().delete(insertedSmsURI,null,null);
                SmsDispatcher.updateThreadId(contact.getThreadId());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dismissCurrThreadNotifs();
                    }
                }).start();
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Bundle bundle = new Bundle();
                bundle.putInt(Contact.THREAD_ID,contact.getThreadId());
                getLoaderManager().initLoader(0, bundle, ConversationActivity.this);
            }
        }.execute();
    }

    private void dismissCurrThreadNotifs() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(contact.getThreadId());
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"ON RESUME!");
        if(contact == null){
            Log.w(TAG,"contact is null");
            return;
        }
        SmsDispatcher.updateThreadId(contact.getThreadId());
        new Thread(new Runnable() {
            @Override
            public void run() {
                dismissCurrThreadNotifs();
                markConversationAsRead(contact);
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SmsDispatcher.updateThreadId(SmsDispatcher.THREAD_ID_NONE);
    }

    private Contact getContact(){
        Bundle extras = getIntent().getExtras();
        try{
            if(extras == null){
                Uri uri = getIntent().getData();
                if(uri == null) return null;
                String scheme = uri.getScheme();
                String schemePart = uri.getSchemeSpecificPart();
                if(scheme == null || schemePart == null) return null;
                if(!scheme.contains("sms") && !scheme.contains("smsto")) return null;

                return new Contact(getBaseContext(),schemePart);
            }else {
                Bundle contactData = extras.getBundle(Constants.CONTACT_BUNDLE);
                if(contactData == null){
                    Log.w(TAG,"contactData is not provided");
                    return null;
                }
                return new Contact(contactData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void markConversationAsRead(final Contact contact){
        if(contact ==null)return;
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
    protected void onStop() {
        super.onStop();

        if(contact == null) return;
        EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
        if(!editText.isFocusable()) return; // not save when it's not default
        SharedPreferences drafts = getSharedPreferences(Constants.DRAFTS_FILE,MODE_PRIVATE);
        SharedPreferences.Editor editor = drafts.edit();

        if(editText.getText() == null || editText.getText().toString().equals("")){
            editor.remove(contact.getAddress());
        }else {
            editor.putString(contact.getAddress(), editText.getText().toString());
        }
        editor.apply();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int thread_id = args.getInt(Contact.THREAD_ID);
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
            if (editText.length() > 0) {
                editText.setText(null);
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private static final int MESSAGE_DEFAULT_SIZE = 160;

    @Override
    public void afterTextChanged(Editable s) {
        if (s == null || s.length() == 0) {
            button.setEnabled(false);
        }else
            button.setEnabled(true);

        if(symbolCounter == null) return;

        if( s != null && s.length()-5 > MESSAGE_DEFAULT_SIZE){
            symbolCounter.setText(s.length()/MESSAGE_DEFAULT_SIZE+" "+
                    s.length()%MESSAGE_DEFAULT_SIZE+"/"+MESSAGE_DEFAULT_SIZE);
        }else{
            symbolCounter.setText("");
        }
    }
}

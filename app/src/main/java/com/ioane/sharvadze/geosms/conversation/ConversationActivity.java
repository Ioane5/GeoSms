package com.ioane.sharvadze.geosms.conversation;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
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

import java.util.ArrayList;
import java.util.Date;

import broadcastReceivers.SmsDispatcher;
import utils.Constants;
import utils.MyActivity;
import utils.Utils;


public class ConversationActivity extends MyActivity implements LoaderManager.LoaderCallbacks<Cursor> ,TextWatcher{

    private static final String TAG = ConversationActivity.class.getSimpleName();

    private ConversationCursorAdapter adapter;
    private ToggleButton webUseToggle;

    /** This is our conversation id, this defines in which conversation we are.*/
    private static int thread_id;

    /** Recipients of the conversation */
    private ArrayList<Contact> contacts;

    private static boolean isKeyboardVisible = false;

    /** If user changed toggle, we set as true */
    private boolean userChangedWebToggle = false;
    private ImageButton button;
    private TextView symbolCounter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        webUseToggle = (ToggleButton)findViewById(R.id.use_web_toggle_button);
        button = (ImageButton)findViewById(R.id.send_button);
        button.setEnabled(false);

        final EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
        symbolCounter = (TextView)findViewById(R.id.symbol_counter);
        editText.addTextChangedListener(this);

        initConversation();
        // if user entered here without contact we disable
        // everything.
        if(contacts == null){
            editText.setEnabled(false);
            editText.setFocusable(false);
            return;
        }

        button.setOnClickListener(new SendButtonListener(contacts));

        // TODO SET TITLE.
        setTitle(TextUtils.isEmpty(contacts.get(0).getName())? contacts.get(0).getAddress(): contacts.get(0).getName());

        adapter = new ConversationCursorAdapter(getBaseContext(),null,true,contacts.get(0));
        listView = (ListView)findViewById(R.id.conversation_list_view);
        listView.setAdapter(adapter);
        //listView.setOnScrollListener(new ListViewKeyboardGestureShow(getBaseContext(),editText));
        listView.setOnTouchListener(new View.OnTouchListener() {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                    if(isKeyboardVisible)
                        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
                return false;
            }
        });

        if(!Utils.isDefaultSmsApp(this)){
            editText.setFocusable(false);
            editText.setHint(R.string.set_default_app_to_send);
            webUseToggle.setEnabled(false);
            button.setEnabled(false);
        }else {
            SharedPreferences prefs = getSharedPreferences(Constants.DRAFTS_FILE,MODE_PRIVATE);
            String text = prefs.getString(Integer.toString(thread_id),null);
            if(!TextUtils.isEmpty(text)) {
                button.setEnabled(true);
                editText.setText(text);
            }
            boolean isChecked = prefs.getBoolean(Constants.TOGGLE_CHECKED,false);
            webUseToggle.setChecked(isChecked);
            webUseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    userChangedWebToggle = !userChangedWebToggle;
                }
            });
        }
        initKeyboardListener();
        startLoader();
    }

    /**
     * This method inits loader when thread_id is resolved.
     */
    private void startLoader(){
        // if we needn't thread_id resolve we don't create new thread.
        if(thread_id != Constants.THREAD_NONE){
            getLoaderManager().initLoader(0, null, this);
            return;
        }
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                // this part resolves thread id.
                String recipientIds = Utils.getRecipientIds(getBaseContext(),contacts);

                Log.i(TAG,"recipientIds " + recipientIds);
                final Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");

                Cursor c = getContentResolver().query(uri, null, Constants.RECIPIENT_IDS + " = ?" ,
                        new String[]{recipientIds} , null);
                if(c.moveToFirst()){
                    thread_id = c.getInt(c.getColumnIndex(Constants.ID));
                    Log.i(TAG,"theradID resolved " + thread_id);
                }else{
                    Log.w(TAG,"we must insert");
                }
                c.close();

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
                getLoaderManager().initLoader(0, null, ConversationActivity.this);
            }
        }.execute();
    }

    private void dismissCurrThreadNotifs() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(thread_id);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"ON RESUME!");
        if(contacts == null){
            Log.w(TAG,"contact is null");
            return;
        }
        SmsDispatcher.updateThreadId(thread_id);
        new Thread(new Runnable() {
            @Override
            public void run() {
                dismissCurrThreadNotifs();
                markConversationAsRead();
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SmsDispatcher.updateThreadId(SmsDispatcher.THREAD_ID_NONE);
    }

    /**
     * This method initializes conversation.
     *
     * This means it resolves in which thread we are.
     * Which contacts are recipients.
     */
    @SuppressWarnings("unchecked cast")
    private void initConversation(){
        try{
            Bundle extras = getIntent().getExtras();

            if(extras != null){
                contacts = (ArrayList<Contact>)extras.getSerializable(Constants.CONTACT_DATA);
                thread_id = extras.getInt(Constants.THREAD_ID,Constants.THREAD_NONE);
            }else{
                Uri uri = getIntent().getData();
                if(uri == null) {
                    Log.w(TAG,"Contact data not provided!");
                    return;
                }
                String scheme = uri.getScheme();
                String schemePart = uri.getSchemeSpecificPart();
                if(scheme == null || schemePart == null) return;
                if(!scheme.contains("sms") && !scheme.contains("smsto")) return;

                contacts = new ArrayList<>(1);
                contacts.add(new Contact(getBaseContext(),schemePart));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void markConversationAsRead(){
        if(thread_id == Constants.THREAD_NONE)
            return;
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
        }.execute(thread_id);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(contacts == null) return;
        EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
        if(!editText.isFocusable()) return; // not save when it's not default
        SharedPreferences drafts = getSharedPreferences(Constants.DRAFTS_FILE,MODE_PRIVATE);
        SharedPreferences.Editor editor = drafts.edit();
        /* if user changed web use toggle , let's save it ,
          to not make user choose on every conversation start...*/
        if(userChangedWebToggle){
            editor.putBoolean(Constants.TOGGLE_CHECKED,webUseToggle.isChecked());
        }
        if(editText.getText() == null || editText.getText().toString().equals("")){
            editor.remove(Integer.toString(thread_id));
        }else {
            editor.putString(Integer.toString(thread_id), editText.getText().toString());
        }
        editor.apply();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG,"mda " + thread_id);
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

        private ArrayList<Contact> contacts;
        private GeoSmsManager smsManager;

        public SendButtonListener(ArrayList<Contact> contacts){
            this.contacts = contacts;
            smsManager = new GeoSmsManager(getBaseContext());
        }
        @Override
        public void onClick(View view) {
            EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
            if(TextUtils.isEmpty(editText.getText())) return;

            String message = editText.getText().toString();
            // it's a draft
            if(contacts == null){
                SMS sms = new SMS(message,new Date(System.currentTimeMillis()), SMS.MsgType.DRAFT,true,false,null);
                smsManager.saveDraft(sms);
            }else{
                SMS sms = new SMS(message,new Date(System.currentTimeMillis()), SMS.MsgType.PENDING,true,false,null);
                smsManager.sendSms(sms,contacts.get(0).getAddress(),thread_id,isSendWeb());
            }
            if (editText.length() > 0) {
                editText.setText(null);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(listView != null){
            int pos = listView.getFirstVisiblePosition();
            outState.putInt("scroll_pos",pos);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int pos = savedInstanceState.getInt("scroll_pos");
        Log.i(TAG,"THIS SHIT IS  = " + pos);
        if(pos != 0)
            listView.smoothScrollToPositionFromTop(pos,0,0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private static final int MESSAGE_DEFAULT_SIZE = 160;

    private static final int MESSAGE_UNICODE_SIZE = 70;


    /**
     * StackOverflow community answer.
     */
    private void initKeyboardListener(){
        final View activityRootView = findViewById(R.id.conversation_activity);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                double ratio =  ((double)activityRootView.getHeight())/activityRootView.getRootView().getHeight();
                // if more than 100 pixels, its probably a keyboard...
                isKeyboardVisible = ratio < 0.75;
            }
        });
    }


    boolean isASCII(String str){
        for(int i=0;i<str.length();i++){
            if(str.charAt(i) >= 128)
                return false;
        }
        return true;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s == null || s.length() == 0) {
            button.setEnabled(false);
        }else
            button.setEnabled(true);

        if(symbolCounter == null) return;
        if(s == null){
            symbolCounter.setText("");
            return;
        }

        int maxLen = MESSAGE_DEFAULT_SIZE;
        if(!isASCII(s.toString()))
            maxLen = MESSAGE_UNICODE_SIZE;

        if(s.length() > maxLen/2){
            symbolCounter.setVisibility(View.VISIBLE);
            String str;
            if(s.length()/maxLen == 0){
                str = String.format("%d/%d",s.length()%maxLen,maxLen);
            }else{
                str = String.format("%d/%d (%d)",s.length()%maxLen,maxLen,s.length()/maxLen);
            }
            symbolCounter.setText(str);
        }else{
            symbolCounter.setText("");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() ==  R.id.action_call){
            if(contacts == null || contacts.size() <= 0)
                return false;
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", contacts.get(0).getAddress(), null));
            startActivity(intent);
            return true;
        }else
            return super.onOptionsItemSelected(item);

    }
}

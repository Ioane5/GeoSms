package com.ioane.sharvadze.geosms.conversation;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.ioane.sharvadze.geosms.MyPreferencesManager;
import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.Utils;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.SMS;
import com.ioane.sharvadze.geosms.websms.AbstractWebSms;

import java.util.Date;


public class ConversationActivity extends MyActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = ConversationActivity.class.getSimpleName();

    private ConversationCursorAdapter adapter;


    private ToggleButton webUseToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        webUseToggle = (ToggleButton)findViewById(R.id.use_web_toggle_button);
        ImageButton button = (ImageButton)findViewById(R.id.send_button);

        Contact contact = getContact();
        button.setOnClickListener(new SendButtonListener(contact));

        setTitle(TextUtils.isEmpty(contact.getName())? contact.getAddress(): contact.getName());

        adapter = new ConversationCursorAdapter(getBaseContext(),null,true,contact);
        ListView listView = (ListView)findViewById(R.id.conversation_list_view);
        listView.setAdapter(adapter);

        if(!Utils.isDefaultSmsApp(this)){
            EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
            editText.setFocusable(false);
            editText.setText(R.string.set_default_app_to_send);
        }

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.RECIPIENT_ID, contact.getId());
        getLoaderManager().initLoader(0,bundle,this);
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



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int thread_id = args.getInt(Constants.RECIPIENT_ID);
        Uri uri = Uri.parse("content://sms");
        Log.i(TAG,"thread_id = "+ thread_id);
        //Telephony.Sms.CONTENT_URI
        return new CursorLoader(getBaseContext(),Uri.parse("content://sms"),null,("thread_id = " + thread_id),null,"date asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        adapter.notifyDataSetChanged();
    }

    private boolean isSendWeb(){
        return webUseToggle.isChecked();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
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
                    getBaseContext());
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

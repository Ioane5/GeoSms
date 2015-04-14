package newConversation;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;

import com.ioane.sharvadze.geosms.R;
import com.tokenautocomplete.TokenCompleteTextView;

import utils.Constants;

public class NewConversationActivity extends ActionBarActivity{


    @SuppressWarnings("unused")
    private static final String TAG = NewConversationActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversaton);

        CursorAdapter adapter = new ContactsCursorAdapter(this, null, 0);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                Uri uri = constraint != null ?
                        Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, constraint.toString()) :
                        ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI;
                // TODO add here custom number row before first cursor...
                return getContentResolver().query(
                        uri, Constants.contacts_projection, null, null,
                        ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
            }
        });

        ContactsCompletionView completionView = (ContactsCompletionView) findViewById(R.id.searchView);
        completionView.setAdapter(adapter);
        completionView.allowDuplicates(false);
        // TODO selected mode here
        completionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Delete);
        completionView.setClickable(true);
    }




}

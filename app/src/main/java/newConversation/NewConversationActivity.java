package newConversation;


import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.ioane.sharvadze.geosms.R;
import com.tokenautocomplete.TokenCompleteTextView;

import utils.Constants;

public class NewConversationActivity extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {


    @SuppressWarnings("unused")
    private static final String TAG = NewConversationActivity.class.getSimpleName();

    private SearchView searchView;
    private CursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversaton);

        adapter = new ContactsCursorAdapter(this, null, 0);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                Uri uri;
                if(TextUtils.isEmpty(constraint))
                    uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                else
                    uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, constraint.toString());

                // TODO add here custom number row before first cursor...
                return getContentResolver().query(
                        uri, Constants.contacts_projection, null, null,
                        ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
            }
        });

        ListView listView = (ListView)findViewById(R.id.contacts_list);
        listView.setAdapter(adapter);
        ContactsCompletionView completionView = (ContactsCompletionView) findViewById(R.id.searchView);


        completionView.allowDuplicates(false);
        completionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.None);
        completionView.setClickable(false);
        completionView.setFocusable(false);

        getLoaderManager().initLoader(0,null,NewConversationActivity.this).forceLoad();
    }

    static String lastQuery = "";

    boolean filter(String s){
        if(lastQuery.equals(s))
            return false;
        lastQuery = s;
        adapter.getFilter().filter(s);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
       return filter(s);
    }
    @Override
    public boolean onQueryTextChange(String s) {
        return filter(s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_conversations, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("To...");
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getBaseContext(),ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                Constants.contacts_projection,null,null,null);
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
}

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
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.ListView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.objects.Contact;

import java.util.ArrayList;

import utils.Constants;

public class NewConversationActivity extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener,AdapterView.OnItemClickListener {


    @SuppressWarnings("unused")
    private static final String TAG = NewConversationActivity.class.getSimpleName();

    private CursorAdapter contactsAdapter;
    private ChosenContactsAdapter chosenContactsAdapter;

    private ArrayList<Contact> chosenContacts;
    private ListView contactsLV;
    private GridView chosenContactsGV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversaton);
        chosenContacts = new ArrayList<>();

        contactsAdapter = new ContactsCursorAdapter(this, null, 0);

        contactsAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                Uri uri;
                if (TextUtils.isEmpty(constraint))
                    uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                else
                    uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, constraint.toString());

                // TODO add here custom number row before first cursor...
                return getContentResolver().query(
                        uri, Constants.contacts_projection, null, null,
                        ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
            }
        });
        contactsLV = (ListView)findViewById(R.id.contacts_list);
        contactsLV.setAdapter(contactsAdapter);
        contactsLV.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        contactsLV.setOnItemClickListener(this);

        chosenContactsGV = (GridView)findViewById(R.id.gridView);
        chosenContactsAdapter = new ChosenContactsAdapter(getBaseContext(),R.layout.contact_bubble);
        chosenContactsGV.setAdapter(chosenContactsAdapter);

        chosenContactsGV.setEmptyView(findViewById(R.id.empty_chosen_contact_view));
        getLoaderManager().initLoader(0,null,NewConversationActivity.this).forceLoad();
    }



    static String lastQuery = "";

    boolean filter(String s){
        if(lastQuery.equals(s))
            return false;
        lastQuery = s;
        contactsAdapter.getFilter().filter(s);

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
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("To...");
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getBaseContext(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                Constants.contacts_projection,null,null,
                ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        contactsAdapter.swapCursor(data);
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        contactsAdapter.swapCursor(null);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("contacts_list", chosenContacts);
    }

    @SuppressWarnings("unchecked cast")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        chosenContacts = (ArrayList<Contact>)savedInstanceState.getSerializable("contacts_list");

        chosenContactsAdapter.addAll(chosenContacts);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) contactsAdapter.getItem(position);
        Log.i(TAG,"clicked contact");
        // TODO add checking here...not with is selected!
        if(contactsLV.isItemChecked(position)){
            chosenContacts.add(contact);
            chosenContactsAdapter.add(contact);
            chosenContactsAdapter.notifyDataSetChanged();
            chosenContactsGV.smoothScrollToPosition(chosenContacts.size()-1);
        }else {
            chosenContacts.remove(contact);
            chosenContactsAdapter.remove(contact);
            chosenContactsAdapter.notifyDataSetChanged();
            chosenContactsGV.smoothScrollToPosition(chosenContacts.size()-1);
        }
    }
}

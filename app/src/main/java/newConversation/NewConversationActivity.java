package newConversation;


import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.objects.Contact;

import java.util.ArrayList;

import utils.Constants;
import utils.Utils;


public class NewConversationActivity extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener,AdapterView.OnItemClickListener {


    @SuppressWarnings("unused")
    private static final String TAG = NewConversationActivity.class.getSimpleName();

    private ContactsCursorAdapter contactsAdapter;
    private ChosenContactsAdapter chosenContactsAdapter;

    private ArrayList<Contact> chosenContacts;
    private GridView chosenContactsGV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversaton);

        contactsAdapter = new ContactsCursorAdapter(this, null, 0);

        chosenContacts = new ArrayList<>();

        ListView contactsLV = (ListView)findViewById(R.id.contacts_list);
        contactsLV.setAdapter(contactsAdapter);
        contactsLV.setOnItemClickListener(this);
        contactsAdapter.setSelectedContacts(chosenContacts);

        chosenContactsGV = (GridView)findViewById(R.id.gridView);
        chosenContactsAdapter = new ChosenContactsAdapter(getBaseContext(),R.layout.contact_bubble);
        chosenContactsGV.setAdapter(chosenContactsAdapter);
        chosenContactsGV.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.i(TAG,"changeint layout");
            }
        });

        chosenContactsGV.setEmptyView(findViewById(R.id.empty_chosen_contact_view));
        getLoaderManager().initLoader(0,null,NewConversationActivity.this).forceLoad();
    }


    static String lastQuery = "";

    boolean filter(String s){
        if(lastQuery.equals(s))
            return false;
        lastQuery = s;

        Bundle bundle = new Bundle();
        bundle.putString("constraint", s);
        getLoaderManager().restartLoader(0, bundle, NewConversationActivity.this);
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
        String constraint = args == null ? null : args.getString("constraint");
        if(constraint != null){
            Uri uri;
            if (TextUtils.isEmpty(constraint))
                uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            else
                uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, constraint);
            // TODO add here custom number row before first cursor...
            return new CursorLoader(getBaseContext(),
                    uri, Constants.contacts_projection, null, null,
                    ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
        }else{
            return new CursorLoader(getBaseContext(),
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    Constants.contacts_projection,null,null,
                    ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
        }
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
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        chosenContacts = (ArrayList<Contact>)savedInstanceState.getSerializable("contacts_list");

        contactsAdapter.setSelectedContacts(chosenContacts);
        chosenContactsAdapter.addAll(chosenContacts);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) contactsAdapter.getItem(position);
        if(contact.getPhotoUri() != null && contact.getPhoto() == null){
            Bitmap image = Utils.getCircleBitmap(Utils.getPhotoFromURI(contact.getPhotoUri(),
                    getBaseContext(), 40));
            contact.setPhoto(image);
        }

        if(!Contact.containsByAddress(chosenContacts, contact.getAddress())){
            chosenContacts.add(contact);
            chosenContactsAdapter.add(contact);

            chosenContactsAdapter.notifyDataSetChanged();
            contactsAdapter.notifyDataSetChanged();
            chosenContactsGV.smoothScrollToPosition(chosenContacts.size()-1);
        }else {
            chosenContacts.remove(contact);
            chosenContactsAdapter.remove(contact);

            chosenContactsAdapter.notifyDataSetChanged();
            contactsAdapter.notifyDataSetChanged();
            chosenContactsGV.smoothScrollToPosition(chosenContacts.size()-1);
        }
    }
}

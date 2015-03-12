package com.ioane.sharvadze.geosms.conversationsList;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.objects.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ioane on 3/10/2015.
 */
public class ConversationsListCAB {


    private Context context;


    private ArrayAdapter<Conversation> listAdapter;

    private static final String TAG = ConversationsListCAB.class.getSimpleName();

    private AlertDialog.Builder builder;

    public ConversationsListCAB(Context context,ListView listView,ArrayAdapter<Conversation> adapter,AlertDialog.Builder builder){
        this.context = context;
        this.listAdapter = adapter;
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MyCABListenter());
        this.builder = builder;
    }

    private class MyCABListenter implements AbsListView.MultiChoiceModeListener{

        SparseBooleanArray checkedItems = new SparseBooleanArray();

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            checkedItems.put(position, checked);
        }
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.conversations_list_cab, menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    int size = checkedItems.size();
                    if(size == 0) return false;
                    builder.setTitle(R.string.warning);
                    builder.setMessage(size == 1? R.string.delete_warning_msg_single :
                            R.string.delete_warning_msg_plural);
                    builder.setPositiveButton("YES", new DatePickerDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... params) {
                                    Log.i(TAG,"doInBakground " + checkedItems.size());
                                    for(int i=0;i<checkedItems.size();i++){
                                        if(checkedItems.valueAt(i)){
                                            int key = checkedItems.keyAt(i);
                                            int threadId = listAdapter.getItem(key).getContact().getThreadId();
                                            Log.i(TAG,"deleting threadId = "  + threadId);
                                            context.getContentResolver().delete(
                                                    Uri.parse("content://mms-sms/conversations"),
                                                    "thread_id=?", new String[] {Integer.toString(threadId)});
                                        }
                                    }
                                    return null;
                                }
                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
                                    Log.i(TAG,"now in OpPostExecute");
                                    List<Conversation> toDel = new ArrayList<Conversation>();
                                    for(int i=0;i<checkedItems.size();i++){
                                        if(checkedItems.valueAt(i)){
                                            int key = checkedItems.keyAt(i);
                                            toDel.add(listAdapter.getItem(key));
                                        }
                                    }
                                    for(Conversation conversation:toDel)
                                        listAdapter.remove(conversation);
                                    checkedItems.clear();
                                }
                            }.execute();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                            dialog.dismiss();
                            checkedItems.clear();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();


                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    checkedItems.clear();
                    return false;
            }
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}

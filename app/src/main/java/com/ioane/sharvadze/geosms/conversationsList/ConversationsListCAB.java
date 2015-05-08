package com.ioane.sharvadze.geosms.conversationsList;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.objects.Conversation;

import java.util.HashMap;

/**
 * Class ConversationsListCAB
 *
 * Created by Ioane on 3/10/2015.
 */
public class ConversationsListCAB {


    private Context context;


    private ArrayAdapter<Conversation> listAdapter;

    @SuppressWarnings("unused")
    private static final String TAG = ConversationsListCAB.class.getSimpleName();

    private AlertDialog.Builder builder;

    public ConversationsListCAB(Context context,ListView listView,ArrayAdapter<Conversation> adapter,AlertDialog.Builder builder){
        this.context = context;
        this.listAdapter = adapter;
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MyCABListenter());
        this.builder = builder;
    }

    // TODO class destroys on rotate, also reference on context.
    // TODO implement this in conversationsList! SHORT IMPLEMENTATION!
    private class MyCABListenter implements AbsListView.MultiChoiceModeListener{

        private  HashMap<Integer,Boolean> checkedItems = new HashMap<>();


        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            if(!checked && checkedItems.containsKey(position))
                checkedItems.remove(position);
            else if(checked)
                checkedItems.put(position, true);

            mode.setTitle(String.format("Selected %d" ,checkedItems.size()));
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
                        public void onClick(DialogInterface dialog, final int which) {
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... params) {
                                    for (Integer toDelete : checkedItems.keySet()) {
                                        long threadId = listAdapter.getItem(toDelete).getId();
                                        context.getContentResolver().delete(
                                                Uri.parse("content://mms-sms/conversations"),
                                                "thread_id=?", new String[]{Long.toString(threadId)});
                                    }

                                    return null;
                                }
                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
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

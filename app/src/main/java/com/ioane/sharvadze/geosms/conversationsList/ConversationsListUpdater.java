package com.ioane.sharvadze.geosms.conversationsList;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.ioane.sharvadze.geosms.objects.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ioane on 3/9/2015.
 */
public class ConversationsListUpdater extends ContentObserver {

    private static List<Integer> updateConversations =  new ArrayList<Integer>();
    private static List<Integer> readConversations =  new ArrayList<Integer>();

    private static String TAG = ConversationsListUpdater.class.getSimpleName();
    private Context ctx;
    private ArrayAdapter<Conversation> listAdapter;

    public ConversationsListUpdater(Context ctx,ArrayAdapter<Conversation> listAdapter){
        super(null);
        this.ctx = ctx;
        this.listAdapter = listAdapter;
        Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
        ctx.getContentResolver().registerContentObserver(uri,true,this);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.i(TAG,"changed DB");
        updateConversations();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.i(TAG,uri == null? "null uri" :uri.toString());
    }



    public static void updateConversation(int updatedThreadId){
        synchronized (updateConversations){
            updateConversations.add(updatedThreadId);
        }
    }

    public static void markAsRead(int threadId) {
        synchronized (readConversations){
            readConversations.add(threadId);
        }
    }

    private void updateConversations(){
        new AsyncTask<Void,Void,List<Conversation>>() {
            @Override
            protected List<Conversation> doInBackground(Void... params) {
                List<Conversation> newConversations = new ArrayList<Conversation>(updateConversations.size());

                synchronized (updateConversations) {
                    if (updateConversations.size() == 0) return null;
                    Log.i(TAG, "updating conversations");
                    Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
                    for (int recipientId : updateConversations) {
                        Cursor c = ctx.getContentResolver().query(uri, null, "_id = ?",
                                new String[]{Integer.toString(recipientId)}, null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                Conversation conversation = new Conversation(ctx, c, true);
                                newConversations.add(conversation);
                            }
                            c.close();
                        }
                    }
                    updateConversations.clear();
                }

                return newConversations;
            }

            @Override
            protected void onPostExecute(List<Conversation> list) {
                super.onPostExecute(list);
                if (listAdapter != null) {
                    if(list != null){
                        for (Conversation con : list) {
                            listAdapter.remove(con);
                            listAdapter.insert(con, 0);
                        }
                    }
                    for (Integer readThreadId : readConversations){
                        for (int i=0;i<listAdapter.getCount();i++){
                            if(readThreadId == listAdapter.getItem(i).getContact().getThreadId()){
                                listAdapter.getItem(i).setMessageRead(true);
                            }
                        }
                    }
                    readConversations.clear();
                    listAdapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }

}

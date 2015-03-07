package com.ioane.sharvadze.geosms.conversationsList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import com.ioane.sharvadze.geosms.objects.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ioane on 2/24/2015.
 */
public class SmartConversationFetcher extends AsyncTask<Void, Void, List<Conversation>>{

    private Context context;
    private int initialLoadingNum;
    private ArrayAdapter<Conversation> adapter;

    private Cursor c;

    /**
     * This class provides use fast startup loading.
     *
     * At first we load initialLoadingNum quantity data
     * to show fast on UI , but than we continue using second AsyncTask to
     * finish loading Data.
     * @param context
     * @param initialLoadingNum how many conversations load at first.
     * @param adapter adapter to fill.
     */
    public SmartConversationFetcher(Context context,int initialLoadingNum,ArrayAdapter<Conversation> adapter){
        this.context = context;
        this.initialLoadingNum = initialLoadingNum;
        this.adapter = adapter;
    }

    @Override
    protected List<Conversation> doInBackground(Void ... params) {
        List<Conversation> list = new ArrayList<Conversation>();
        Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
        c = context.getContentResolver().query(uri, null, null, null, "date desc");
        int i = 0;
        while(c.moveToNext()){
            if(i++ >= initialLoadingNum) break;
            list.add(new Conversation(context,c,true));
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<Conversation> conversations) {
        super.onPostExecute(conversations);
        adapter.addAll(conversations);
        if(c.isAfterLast()) {
            c.close();
            return; // we already loaded data.
        }


        // This task finishes loading...
        new AsyncTask<Cursor, Void, List<Conversation>>() {
            @Override
            protected List<Conversation> doInBackground(Cursor... params) {
                List<Conversation> list = new ArrayList<Conversation>();
                Cursor c = params[0];
                while (c.moveToNext()){
                    list.add(new Conversation(context,c,true));
                }
                c.close();
                return list;
            }
            @Override
            protected void onPostExecute(List<Conversation> conversations) {
                super.onPostExecute(conversations);
                adapter.addAll(conversations);
            }
        }.execute(c);
    }
}

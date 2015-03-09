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
public class SmartConversationFetcher extends AsyncTask<Void,  List<Conversation>,Void>{

    private Context context;
    private int initialLoadingNum;
    private ArrayAdapter<Conversation> adapter;
    private static final Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");

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
    protected Void doInBackground(Void ... params) {
        List<Conversation> list = new ArrayList<Conversation>();
        // listen for db changes.
        c = context.getContentResolver().query(uri, null, null, null, "date desc");
        int i = 0;
        while(c.moveToNext()){
            list.add(new Conversation(context, c, true));
            if(++i % initialLoadingNum == 0) {
                publishProgress(list);
                list = new ArrayList<Conversation>();
            }
        }
        publishProgress(list);
        c.close();
        return null;
    }

    @Override
    protected void onProgressUpdate(List<Conversation>... values) {
        super.onProgressUpdate(values);
        List<Conversation> part = values[0];
        adapter.addAll(part);
    }


}

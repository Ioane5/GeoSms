package com.ioane.sharvadze.geosms.conversationsListFatchers;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;

import utils.Constants;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ioane on 3/12/2015.
 */
public class ConversationLoader extends AsyncTaskLoader<List<Conversation>> {

    private static final String TAG = ConversationLoader.class.getSimpleName();

    private List<Conversation> mConversations;
    private SparseArray<Contact> mContactCache;

    private static final Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");

    private ConversationsContentObserver mConversObserver;

    public ConversationLoader(Context context) {
        super(context);
        mContactCache = new SparseArray<Contact>();
    }

    @Override
    public List<Conversation> loadInBackground() {
        mConversations = new ArrayList<Conversation>();
        if(mContactCache == null)
            mContactCache = new SparseArray<Contact>();

        Cursor c = getContext().getContentResolver().query(uri, null, null, null, "date desc");
        while (c.moveToNext()) {
            int numMsg = c.getInt(c.getColumnIndex(Constants.MSG_COUNT));
            if(numMsg <= 0) continue; // we don't need empty conversations
            Conversation conversation = new Conversation(getContext(), c,mContactCache);
            mConversations.add(conversation);
        }
        c.close();
        return mConversations;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(List<Conversation> convers) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (convers != null) {
                onReleaseResources(convers);
            }
        }
        List<Conversation> oldConvers = mConversations;
        mConversations = convers;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(convers);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldConvers != null) {
            onReleaseResources(oldConvers);
        }
    }


    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (mConversations != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mConversations);
        }
        // Start watching for changes in the conversation data.
        if (mConversObserver == null) {
            mConversObserver = new ConversationsContentObserver(this);
        }
        // TODO maybe check here for new data available

        if (takeContentChanged() || mConversations == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(List<Conversation> convers) {
        super.onCanceled(convers);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(convers);
    }


    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mConversations != null) {
            onReleaseResources(mConversations);
            mConversations = null;
        }

        // Stop monitoring for changes.
        if (mConversObserver != null) {
            // TODO maybe receiver is better?
            // TODO getContext().unregisterReceiver(mConversObserver);
            mConversObserver = null;
        }
    }

    private void onReleaseResources(List<Conversation> conversations) {
        //if(mContactCache != null) mContactCache.clear();
    }

}

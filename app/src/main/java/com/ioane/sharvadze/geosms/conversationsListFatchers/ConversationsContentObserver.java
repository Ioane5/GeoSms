package com.ioane.sharvadze.geosms.conversationsListFatchers;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by Ioane on 3/12/2015.
 */
public class ConversationsContentObserver extends ContentObserver {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public ConversationsContentObserver(Handler handler) {
        super(handler);
    }

    private ConversationLoader conversationLoader;
    private Handler handler;

    private Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");


    public ConversationsContentObserver(ConversationLoader conversationLoader) {
        super(null);
        this.conversationLoader = conversationLoader;
        conversationLoader.getContext().getContentResolver().registerContentObserver(uri,true,this);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        conversationLoader.onContentChanged();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
    }
}

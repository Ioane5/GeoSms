package com.ioane.sharvadze.geosms.objects;

import android.content.Context;
import android.database.Cursor;

import com.ioane.sharvadze.geosms.Constants;

import java.util.Date;

/**
 * Created by Ioane on 2/21/2015.
 */
public class Conversation {

    private static final String RECIPIENT_ID = Constants.RECIPIENT_ID;

    private static final String LAST_MESSAGE = Constants.CONVERSATION_LAST_MESSAGE;

    private static final String DATE = Constants.CONVERSATION_DATE;

    private static final String IS_READ = Constants.CONVERSATION_READ;



    /**
     * Conversation with
     */
    private Contact contact;

    /**
     * Recipient Id
     */
    private int recipientId;

    /**
     *  Last Message
     */
    private String lastMessage;

    /**
     * Conversation date
     */
    private Date date;

    private  boolean messageRead;


    /**
     * This Constructor creates new Conversation object
     * from Cursor.
     *
     * Queries database for resolving Contact , if resolveContact is true.
     *
     * @param context
     * @param cursor
     * @param resolveContact
     */
    public Conversation(Context context,Cursor cursor,boolean resolveContact){
        this.recipientId = cursor.getInt(cursor.getColumnIndex(RECIPIENT_ID));
        this.lastMessage = cursor.getString(cursor.getColumnIndex(LAST_MESSAGE));
        this.date = new Date(cursor.getLong(cursor.getColumnIndex(DATE)));
        this.messageRead = cursor.getInt(cursor.getColumnIndex(IS_READ)) == 1;

        if(resolveContact){
            this.contact = new Contact(context,recipientId);
        }
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public boolean isMessageRead() {
        return messageRead;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }
}

package com.ioane.sharvadze.geosms.objects;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.ioane.sharvadze.geosms.Constants;
import com.ioane.sharvadze.geosms.Utils;

import java.io.Serializable;

/**
 * Created by Ioane on 2/21/2015.
 */
public class Contact implements Serializable{

    private int id;

    private int threadId;

    private String name;

    private String photoUri;

    private String address;

    private Bitmap photo;


    private static final String TAG = Contact.class.getSimpleName();

    public Contact(int threadId, String name, String photoUri,String address,Bitmap photo) {
        this.threadId = threadId;
        this.name = name;
        this.photoUri = photoUri;
        this.address = address;
        this.photo = photo;
    }

    private static final String[] projection = {
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    };

    private static final String ID = "id";
    private static final String THREAD_ID = "thread_id";
    private static final String NAME = "name";
    private static final String PHOTO = "photo";
    private static final String PHOTO_URI = "photo_uri";
    private static final String ADDRESS = "address";

    public Contact(Bundle contactData){
        this.threadId = contactData.getInt(THREAD_ID);
        this.id = contactData.getInt(ID);
        this.name = contactData.getString(NAME);
        this.photoUri =  contactData.getString(PHOTO_URI);
        this.address = contactData.getString(ADDRESS);
        this.photo = (Bitmap)contactData.getParcelable(PHOTO);
    }

    public Bundle getBundle(){
        Bundle bundle = new Bundle();
        bundle.putInt(ID,id);
        bundle.putInt(THREAD_ID,threadId);
        bundle.putString(NAME,name);
        bundle.putString(PHOTO_URI,photoUri);
        bundle.putString(ADDRESS,address);
        bundle.putParcelable(PHOTO,photo);
        return bundle;
    }

    private void initFromAddress(Context context,String address){
        setAddress(address);
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        Cursor c = context.getContentResolver().query(lookupUri, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            String displayName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String photoURI = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            int id = c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));

            setId(id);
            setName(displayName);
            setPhotoUri(photoURI);
            setPhoto(getPhotoFromURI(photoURI,context));
        }
        c.close();
    }

    public Contact(Context context,String address){
        initFromAddress(context,address);
//        //  get id from address...
//        Cursor c = context.getContentResolver().query(
//                Uri.parse("content://mms-sms/canonical-addresses"), new String[]{"_id"},
//                "address = ? ", new String[]{address}, null);
//        if (c != null) {
//            if (c.getCount() != 0) {
//
//                c.moveToNext();
//                int id = c.getInt(c.getColumnIndex("_id"));
//                Log.i(TAG,"ID IS " + id);
//                setThreadId(id);
//            }
//            c.close();
//        }
    }


    public Contact(Context context,int recipientId,int threadId){
        setThreadId(threadId);
        setAddress(null);
        setName(null);
        setPhotoUri(null);

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContentUris.withAppendedId(Uri.parse("content://mms-sms/canonical-address"), recipientId),
                null, null, null, null);
        if(c.moveToFirst()){
            String address = c.getString(0);
            if(address != null)
                address = Utils.removeWhitespaces(address);
                initFromAddress(context,address);
        }

        c.close();

    }

    private Bitmap getPhotoFromURI(String photoURI,Context context) {
        if(photoURI == null) return null;
        try {
            return Utils.getCircleBitmap(Utils.getPhotoFromURI(photoURI,context, Constants.IMAGE_SIZE));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int id) {
        this.threadId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id= " + id +
                ", threadId=" + threadId +
                ", name='" + name + '\'' +
                ", photoUri='" + photoUri + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

}

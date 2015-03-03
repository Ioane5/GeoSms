package com.ioane.sharvadze.geosms.objects;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.ioane.sharvadze.geosms.Utils;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Ioane on 2/21/2015.
 */
public class Contact implements Serializable{

    private int id;

    private String name;

    private String photoUri;

    private String address;

    private Bitmap photo;


    private static final String TAG = Contact.class.getSimpleName();

    public Contact(int id, String name, String photoUri,String address,Bitmap photo) {
        this.id = id;
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
    private static final String NAME = "name";
    private static final String PHOTO = "photo";
    private static final String PHOTO_URI = "photo_uri";
    private static final String ADDRESS = "address";

    public Contact(Bundle contactData){
        this.id = contactData.getInt(ID);
        this.name = contactData.getString(NAME);
        this.photoUri =  contactData.getString(PHOTO_URI);
        this.address = contactData.getString(ADDRESS);
        this.photo = (Bitmap)contactData.getParcelable(PHOTO);
    }

    public Bundle getBundle(){
        Bundle bundle = new Bundle();
        bundle.putInt(ID,id);
        bundle.putString(NAME,name);
        bundle.putString(PHOTO_URI,photoUri);
        bundle.putString(ADDRESS,address);
        bundle.putParcelable(PHOTO,photo);
        return bundle;
    }
    public Contact(Context context,int recipientId){
        setId(recipientId);
        setAddress(null);
        setName(null);
        setPhotoUri(null);

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContentUris.withAppendedId(Uri.parse("content://mms-sms/canonical-address"), recipientId),
                null, null, null, null);

        if(c.moveToFirst()){
            String address = c.getString(0);
            c.close();
            if(address != null) {
                setAddress(address);
                Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
                c = context.getContentResolver().query(lookupUri, null, null, null, null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    String displayName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String photoURI = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    setName(displayName);
                    setPhotoUri(photoURI);
                    setPhoto(getPhotoFromURI(photoURI,context));
                }
                c.close();
            }
        }else
            c.close();

    }

    private Bitmap getPhotoFromURI(String photoURI,Context context) {
        if(photoURI == null) return null;
        try {
            // get image from filesystem
            InputStream input = context.getContentResolver().openInputStream(Uri.parse(photoURI));

            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 8;
            // reduce quality
            Bitmap bitmap = BitmapFactory.decodeStream(input,null,options);
            // return circled image bitmap
            return Utils.getCircleBitmap(bitmap);
        } catch (Exception e) {
            Log.w(TAG,"exception on decoding contact photo");
            e.printStackTrace();
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
                "id=" + id +
                ", name='" + name + '\'' +
                ", photoUri='" + photoUri + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

}

package newConversation;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AlphabetIndexer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.objects.Contact;

import java.util.ArrayList;

import utils.Utils;

/**
 * Class ContactsCursorAdapter
 * Custom cursor adapter for contact item binding.
 *
 * Created by Ioane on 4/14/2015.
 */
public class ContactsCursorAdapter extends CursorAdapter implements SectionIndexer{

    @SuppressWarnings("unused")
    private static final String TAG = ContactsCursorAdapter.class.getSimpleName();

    private AlphabetIndexer indexer;

    private Bitmap DEFAULT_IMAGE;

    private Drawable SELECTED_CONTACT_IMAGE;

    private ArrayList<Contact> mSelectedContacts;

    public ContactsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        DEFAULT_IMAGE = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_no_image);
        DEFAULT_IMAGE = Utils.getCircleBitmap(DEFAULT_IMAGE);

        SELECTED_CONTACT_IMAGE = context.getResources().getDrawable(R.drawable.selected_contact_image);

        indexer = new AlphabetIndexer(null, 0 , " ABCDEFGHIJKLMNOPQRTSUVWXYZ0123456789");
        mSelectedContacts = null;
    }

    public void setSelectedContacts(ArrayList<Contact> selectedContacts){
        mSelectedContacts = selectedContacts;
    }

    public void addSelectedContact(Contact contact){
        if(mSelectedContacts != null)
            mSelectedContacts.add(contact);
    }

    public void removeSelectedContact(Contact contact){
        if(mSelectedContacts != null)
            mSelectedContacts.remove(contact);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null? "no contact," :
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))+
                        " "+ cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }


    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if(newCursor != null && !newCursor.isClosed()){

            indexer = new AlphabetIndexer(newCursor,
                    newCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                    " ABCDEFGHIJKLMNOPQRTSUVWXYZ0123456789");
        }else{
            indexer.setCursor(newCursor);
        }
        return super.swapCursor(newCursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
    }

    @Override
    public Object[] getSections() {
        if(indexer == null) return null;
        return indexer.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if(indexer == null) return 0;
        return indexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
       if(indexer == null) return 0;
       return indexer.getSectionForPosition(position);
    }

    private boolean isSelected(String address){
        if(mSelectedContacts != null){
            for(int i=0;i<mSelectedContacts.size();i++){
                Contact cmp = mSelectedContacts.get(i);
                if(TextUtils.equals(address,cmp.getAddress()))
                    return true;
            }
        }
        return false;
    }

    private static class ViewHolder{
        TextView nameView;
        TextView phoneKindView;
        TextView phoneNumberView;
        ImageView contactPhotoView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder)view.getTag();

        if (holder == null) {
            holder = new ViewHolder();
            holder.nameView = (TextView)view.findViewById(R.id.contact_name);
            holder.phoneKindView = (TextView)view.findViewById(R.id.phone_kind_text_view);
            holder.phoneNumberView = (TextView)view.findViewById(R.id.phone_number_text_view);
            holder.contactPhotoView = (ImageView)view.findViewById(R.id.contact_picture_image_view);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }

        int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
        CharSequence phoneType = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, null);
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));


        holder.nameView.setText(name);
        holder.phoneKindView.setText(phoneType);
        holder.phoneNumberView.setText(phoneNumber);

        holder.contactPhotoView.setTag(cursor.getPosition());

        if(isSelected(phoneNumber)){
            holder.contactPhotoView.setImageDrawable(SELECTED_CONTACT_IMAGE);
        }else{
            if(photoUri != null){
                holder.contactPhotoView.setImageBitmap(null);
                setImage(holder.contactPhotoView, photoUri, context, cursor.getPosition());
            }else
                holder.contactPhotoView.setImageBitmap(DEFAULT_IMAGE);
        }



    }

    @Override
    public Object getItem(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
        return new Contact(-1,name,photoUri,phoneNumber,null);
    }


    int cacheSize = 4 * 1024 * 1024; // 4MiB
    private final LruCache<String,Bitmap> mBitmapCache = new LruCache<String,Bitmap>(cacheSize){
        protected int sizeOf(String key, Bitmap value){
            return value.getByteCount();
        }
    };

    private void setImage(final ImageView imageView,String photoUri,Context ctx,int pos){
        synchronized (mBitmapCache) {
            Bitmap bitmap = mBitmapCache.get(photoUri);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }

        new AsyncTask<Object, Void, Bitmap>() {
            private ImageView v;
            private int position;

            @Override
            protected Bitmap doInBackground(Object... params) {
                v = (ImageView)params[0];
                String photoUri = (String)params[1];
                Context ctx = (Context)params[2];
                position = (Integer)params[3];
                Bitmap bitmap = Utils.getCircleBitmap(Utils.getPhotoFromURI(photoUri, ctx, 80));
                synchronized (mBitmapCache) {
                    mBitmapCache.put(photoUri,bitmap);
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                if ((Integer)v.getTag() == position) {
                    // If this item hasn't been recycled already, hide the
                    // progress and set and show the image
                    v.setVisibility(View.VISIBLE);
                    v.setImageBitmap(result);
                }
            }
        }.execute(imageView,photoUri,ctx,pos);
    }

}

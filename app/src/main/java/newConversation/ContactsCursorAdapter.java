package newConversation;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.objects.Contact;

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

    public ContactsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null? "no contact," :
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))+
                        " "+ cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }


    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if(indexer == null){
            indexer = new AlphabetIndexer(newCursor,
                    newCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                    "ABCDEFGHIJKLMNOPQRTSUVWXYZ0123456789");
        }else{
            indexer.setCursor(newCursor);
        }

        return super.swapCursor(newCursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contact_item,parent,false);
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
        CharSequence phoneType = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(),type,null);
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

        holder.nameView.setText(name);
        holder.phoneKindView.setText(phoneType);
        holder.phoneNumberView.setText(phoneNumber);

        holder.contactPhotoView.setTag(cursor.getPosition());

        if(photoUri != null){
            setImage(holder.contactPhotoView,photoUri,context,cursor.getPosition());
        }else
            holder.contactPhotoView.setImageBitmap(null);


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

    private void setImage(final ImageView imageView,String photoUri,Context ctx,int pos){
        new AsyncTask<Object, Void, Bitmap>() {
            private ImageView v;
            private int position;

            @Override
            protected Bitmap doInBackground(Object... params) {
                v = (ImageView)params[0];
                String photoUri = (String)params[1];
                Context ctx = (Context)params[2];
                position = (Integer)params[3];
                return Utils.getCircleBitmap(Utils.getPhotoFromURI(photoUri, ctx, 40));
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

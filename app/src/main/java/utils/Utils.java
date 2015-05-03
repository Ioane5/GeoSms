package utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.util.Log;

import com.ioane.sharvadze.geosms.objects.Contact;
import com.ioane.sharvadze.geosms.objects.Conversation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Class Utils
 *
 * Created by Ioane on 2/25/2015.
 */
@SuppressLint("NewApi")
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    private static final String CONTACT_LIST_FILE = "file_contact_list";

    public static void saveContactList(Context context,ArrayList<Conversation> contactList){
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(CONTACT_LIST_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(contactList);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked cast")
    public static ArrayList<Conversation> loadContactList(Context context){
        FileInputStream fis;
        ArrayList<Conversation> obj = null;
        try {
            fis = context.openFileInput(CONTACT_LIST_FILE);
            ObjectInputStream is = new ObjectInputStream(fis);
            obj = (ArrayList<Conversation>) is.readObject();
            is.close();
            fis.close();
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG,"serialisation exception");
        }
        return obj;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap){
        // create as circle.
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int pixels = Constants.IMAGE_SIZE;
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;

    }

    public static boolean isDefaultSmsApp(Context context){
        final String myPackageName = context.getPackageName();

        if(Build.VERSION.SDK_INT >= Constants.KITKAT_API_LEVEL){
            if(!Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName))
                return false;
        }
        return true;
    }


    public static Bitmap getPhotoFromURI(String photoURI,Context context,int size) {
        if(photoURI == null) return null;
        try {
            // get image from filesystem
            InputStream input = context.getContentResolver().openInputStream(Uri.parse(photoURI));

            return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input, null, null),size,size,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static int getSmsThreadId(Context ctx,Uri uri) {
//        Cursor c = ctx.getContentResolver().query(uri,new String[]{Constants.THREAD_ID},null,null,null);
//        if(c.moveToFirst()){
//            Integer i = c.getInt(c.getColumnIndex(Constants.THREAD_ID));
//            c.close();
//            return i;
//        }else c.close();
//
//        return 0;
//    }


    public static String removeWhitespaces(String address) {
        return address.replaceAll("\\s+", "");
    }



    /**
     * Returns header from contacts.
     *
     * @param contacts list from which header is cereated.
     * @return header
     */
    public static String getChatHeader(ArrayList<Contact> contacts){
        StringBuilder header = new StringBuilder();
        for(int i=0;i<contacts.size();i++){
            Contact c = contacts.get(i);
            header.append(c.getName() != null ? c.getName() : c.getAddress());
            if(i != contacts.size() -1)
                header.append(", ");
        }
        return header.toString();
    }









}

package com.ioane.sharvadze.geosms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.provider.Telephony;

/**
 * Created by Ioane on 2/25/2015.
 */
@SuppressLint("NewApi")
public class Utils {

    public static Bitmap getCircleBitmap(Bitmap bitmap){
        // create as circle.
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int pixels = 1000;
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

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
}

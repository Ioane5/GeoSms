package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Class for downloading images in background
 *
 * Created by ioane on 5/2/15.
 */
public class AsyncImageDownloader {

    private BlockingQueue<ImageInfo> mQueue;
    private static Boolean mActive;
    private Context mContext;
    private Handler mHandler;


    int cacheSize = 4 * 1024 * 1024; // 4MiB
    private final LruCache<String,Bitmap> mBitmapCache = new LruCache<String,Bitmap>(cacheSize){
        protected int sizeOf(String key, Bitmap value){
            return value.getByteCount();
        }
    };

    public AsyncImageDownloader(Context context) {
        mQueue = new LinkedBlockingQueue<>();
        mActive = false;
        mContext = context;
        mHandler = new ImageSetter(mContext.getMainLooper());
    }

    public void addImage(String url,ImageView imageView){
        ImageInfo task = new ImageInfo();
        task.url = url;
        task.imageViewWeakReference = new WeakReference<>(imageView);

        Bitmap cachedBitmap = mBitmapCache.get(url);
        // if we have in cache , we directly set.
        if(cachedBitmap != null){
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        mQueue.add(task);
        if(!mActive){
            new Thread(new DownloadExecutor()).start();
        }

    }


    private class DownloadExecutor implements Runnable{

        /** how many seconds to wait for thread */
        private static final long TIME_TO_WAIT = 2;

        @Override
        public void run() {
            while (true){
                try {
                    ImageInfo task = mQueue.poll(TIME_TO_WAIT, TimeUnit.SECONDS);
                    if(task == null){
                        mActive = false;
                        break; // waited enough , now stopping thread.
                    }

                    // new task arrived.
                    Bitmap bitmap = Utils.getCircleBitmap(Utils.getPhotoFromURI(task. url, mContext, 80));

                    synchronized (mBitmapCache){
                        mBitmapCache.put(task.url,bitmap);
                    }
                    ImageView imageView;
                    if(task.imageViewWeakReference != null && (imageView = task.imageViewWeakReference.get()) != null){
                        FinishedTask finishedTask = new FinishedTask();
                        finishedTask.imageView = imageView;
                        finishedTask.bitmap = bitmap;
                        finishedTask.url = task.url;
                        // update imageView with bitmap
                        mHandler.obtainMessage(0, finishedTask).sendToTarget();
                    }

                } catch (InterruptedException e) {
                    // thread was interrupted.
                    mActive = false;
                    break;
                }
            }

        }
    }


    private static class ImageSetter extends Handler{

        public ImageSetter(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            FinishedTask finishedTask = (FinishedTask)msg.obj;
            // if imageview isn't recycled
            if(TextUtils.equals((CharSequence) finishedTask.imageView.getTag(),finishedTask.url)){
                finishedTask.imageView.setImageBitmap(finishedTask.bitmap);
            }
            super.handleMessage(msg);
        }
    }

    private static class FinishedTask {
        ImageView imageView;
        Bitmap bitmap;
        String url;
    }

    private static class ImageInfo {
        WeakReference<ImageView> imageViewWeakReference;
        String url;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImageInfo imageInfo = (ImageInfo) o;

            return !(url != null ? !url.equals(imageInfo.url) : imageInfo.url != null);
        }

        @Override
        public int hashCode() {
            return url != null ? url.hashCode() : 0;
        }
    }
}

package xcxin.filexpert.Thumbnails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
 * This class is to process the thumb of the image in a single thread,
 * you can handle you UI to implement thumbDone().
 * Of course, you'd better call stopThumbGetter() when you quit your
 * application.
 * 
 * @author Swandle <swandle@gmail.com>
 */
public abstract class ThumbGetter {
	
	public final static String LOG_TAG = "ThumbGetter";
	
	private final static int THUMB_DONE = 1;
	private final static int THUMB_CACHE = 30;
	
	private boolean stopThread = false;
	private static boolean stopAllThread = false;

	private List<String> mImageFileList;
	private static HashMap<String, Drawable> mThumbCache;
	private List<String> mThumbCacheList;
	
	private ThumbGetterThread mThumbGetter;
	
	private final ThumbGetterHandler mHandler;
	private final Context mContext;	
	
	public abstract void onThumbDone(String path, Drawable thumb);
	
	public ThumbGetter(Context ctx){
		mImageFileList = new ArrayList<String>();
		
		if (mThumbCache == null)
			mThumbCache = new HashMap<String, Drawable>();
		
		mThumbCacheList = new ArrayList<String>();
		
		mHandler = new ThumbGetterHandler();
		mContext = ctx;
	}
	
	public Drawable getThumb(String pathName) {
		if (mThumbCache.containsKey(pathName)) {
            return mThumbCache.get(pathName);
        }

    	return null;
	}
	
	/**
	 * If there is a running thread to get thumbs, and you don't want to cancel
	 * it, remain it. or else create a new thread to get thumbs.
	 * 
	 * @param cancelLastGetter
	 *            cancel the last running thread to get thumbs or not
	 */
	public void start(String pathName, boolean cancelLastGetter) {
		
		boolean isRunning = false;
		
		if (mThumbGetter != null
				&& mThumbGetter.getStatus() == AsyncTask.Status.RUNNING)
			isRunning = true;
		
		addToImageList(pathName);
		
		if (isRunning && !cancelLastGetter)
			return;
		
		if (cancelLastGetter && isRunning) {
			stopThread = true;

			synchronized (mImageFileList) {
				mImageFileList.clear();
			}
		}

		while (mThumbGetter != null
				&& mThumbGetter.getStatus() == AsyncTask.Status.RUNNING) {
//			try {
//				synchronized(mThumbGetter) {
//					mThumbGetter.wait(50);
//				}
//			} catch (InterruptedException e) {
//				Log.d(LOG_TAG, "", e);
//			}
		}

		stopThread = false;
		stopAllThread = false;
		mThumbGetter = new ThumbGetterThread();
		mThumbGetter.execute();
	}
	
	public void stop() {
		stopThread = true;
		synchronized (mImageFileList) {
			mImageFileList.clear();
		}
	}
	
	public static void stopAll() {
		stopAllThread = true;
	}
	
	/**
	 * Add the pathName into the image list to get thumbs.
	 * 
	 * @param pathName the image full path and file name
	 * @return 0 if there has been pathName in the list, or return 1.
	 */
	private int addToImageList(String pathName) {
		synchronized (mImageFileList) {
			if (mImageFileList.contains(pathName))
				return 0;
			else {
				mImageFileList.add(pathName);
				return 1;
			}
		}
	}
	
	private void putInThumbCache(String pathName, Drawable thumb) {
		if (mThumbCache.size() > THUMB_CACHE && mThumbCacheList.size() > 0) {
			mThumbCache.remove(mThumbCacheList.get(0));
			mThumbCacheList.remove(0);
		}

		mThumbCache.put(pathName, thumb);
		mThumbCacheList.add(pathName);
	}
	
	private class ThumbFile {
		public Drawable thumb;
		public String pathName;
		
		public ThumbFile(String path, Drawable thumb) {
			this.pathName = path;
			this.thumb = thumb;
		}
	}
	
	private class ThumbGetterHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {			
			switch (msg.what) {
			case THUMB_DONE:
				ThumbFile thumbFile = (ThumbFile)msg.obj;
				onThumbDone(thumbFile.pathName, thumbFile.thumb);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	/**
	 * Add the image file in a queue, and get the thumb one by one int a thread.
	 * But is it the best way? Maybe one in one is better than one by one.
	 * So, every picture is in a single thread or all picture in the same thread?
	 * 
	 * @author Swandle
	 */
	private class ThumbGetterThread extends AsyncTask<Object, Object, Object> {  
	    @Override
	    protected void onPostExecute(Object param) {
	    	stopThread = false;
	    }

	    @Override
	    protected Object doInBackground(Object...params) {
	    	String pathName = null;
	    	
	    	while (!stopThread && !stopAllThread) {
				synchronized (mImageFileList) {
					if (mImageFileList != null && !mImageFileList.isEmpty()) {
						pathName = mImageFileList.get(0);
						mImageFileList.remove(0);
					} else {
						stopThread = true;
						continue;
					}
				}
				
				Drawable thumb = ThumbnailUtils.getThumbOrApkIcon(mContext, pathName);

				if (thumb != null) {
					Message msg = mHandler.obtainMessage(THUMB_DONE);
					msg.obj = new ThumbFile(pathName, thumb);
					mHandler.sendMessage(msg);

					putInThumbCache(pathName, thumb);
				}
			}
	    	
	    	return null;
	    }
	}
}

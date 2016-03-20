package xcxin.filexpert.Thumbnails;

import java.lang.reflect.Method;

import xcxin.filexpert.FileOperator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore.Images;
import android.util.Log;

public abstract class ThumbMedia {
	
	private final static String TAG = "ThumbMedia";
	private static ThumbMedia mThumbMedia;

	public static ThumbMedia getThumbManager() {
		if (mThumbMedia == null) {			
//			if (Build.VERSION.SDK_INT < 8) {
				mThumbMedia = new VideoThumbFroyo();
//			} else
//				mThumbMedia = new VideoThumbCupcake();
		}
		
		return mThumbMedia;
	}
	
	public Bitmap getMediaThumb(String filePath, int kind) {
		if (FileOperator.isVideoFile(filePath))
			return getVideoThumb(filePath, kind);
		else if (FileOperator.isAudioFile(filePath))
			return getMusicThumb(filePath, kind);
		else
			return null;
	}
	
	public abstract Bitmap getVideoThumb(String filePath, int kind);
	public abstract Bitmap getMusicThumb(String filePath, int kind);
	
	@SuppressWarnings("unused")
	private static class VideoThumbCupcake extends ThumbMedia {

		@Override
		public Bitmap getVideoThumb(String filePath, int kind) {
			return null;
		}

		@Override
		public Bitmap getMusicThumb(String filePath, int kind) {
			return null;
		}		
	}
	
	
	private static class VideoThumbFroyo extends ThumbMedia {

		public static final int MODE_GET_METADATA_ONLY  = 0x01;
		public static final int MODE_CAPTURE_FRAME_ONLY = 0x02;
		
		@Override
		public Bitmap getVideoThumb(String filePath, int kind) {
//			return android.media.ThumbnailUtils.createVideoThumbnail(filePath, kind);
			try {
				Class<?> hideClass = Class.forName("android.media.MediaMetadataRetriever");

				Method setMode = null;
				for (Method method : hideClass.getMethods()) {
					if (method.getName().equals("setMode")) {
						setMode = method;
					}
				}

				if (setMode == null)
					return null;

				Object o = hideClass.newInstance();

				Method captureFrame = hideClass.getMethod("captureFrame");
				Method setDataSource = hideClass.getMethod("setDataSource",
						String.class);

				setMode.invoke(o, MODE_CAPTURE_FRAME_ONLY);
				setDataSource.invoke(o, filePath);
				byte[] art = (byte[]) captureFrame.invoke(o);
				Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
				
				if (kind == Images.Thumbnails.MICRO_KIND) {
					bitmap = ThumbnailUtils.extractThumbnail(bitmap,
							ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL,
							ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL,
							ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
				}

				return bitmap;

			} catch (ClassNotFoundException e) {
				Log.d(TAG, "Exception", e);
			} catch (Exception e) {
			}

			return null;
		}

		@Override
		public Bitmap getMusicThumb(String filePath, int kind) {
			try {
				Class<?> hideClass = Class.forName("android.media.MediaMetadataRetriever");

				Method setMode = null;
				for (Method method : hideClass.getMethods()) {
					if (method.getName().equals("setMode")) {
						setMode = method;
					}
				}

				if (setMode == null)
					return null;

				Object o = hideClass.newInstance();

				Method extractAlbumArt = hideClass.getMethod("extractAlbumArt");
				Method setDataSource = hideClass.getMethod("setDataSource",
						String.class);

				setMode.invoke(o, MODE_GET_METADATA_ONLY);
				setDataSource.invoke(o, filePath);
				byte[] art = (byte[]) extractAlbumArt.invoke(o);
				Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
				
				if (kind == Images.Thumbnails.MICRO_KIND) {
					bitmap = ThumbnailUtils.extractThumbnail(bitmap,
							ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL,
							ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL,
							ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
				}

				return bitmap;

			} catch (ClassNotFoundException e) {
				Log.d(TAG, "Exception", e);
			} catch (Exception e) {
			}

			return null;
		}
	}
}

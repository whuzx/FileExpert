package xcxin.filexpert.Thumbnails;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;

import xcxin.filexpert.FeFile;
import xcxin.filexpert.FileOperator;
import xcxin.filexpert.SysInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;

/**
 * This class is modified mainly from Android2.2. Only the method
 * getThumbFromSystem() is written by Swandle
 */
public class ThumbnailUtils {
	private static final String TAG = "ThumbnailUtils";
	/* Maximum pixels size for created bitmap. */
	private static final int MAX_NUM_PIXELS_THUMBNAIL = 512 * 384;
	private static final int MAX_NUM_PIXELS_MICRO_THUMBNAIL = 60 * 60;
	private static final int UNCONSTRAINED = -1;
	/* Options used internally. */
	private static final int OPTIONS_NONE = 0x0;
	private static final int OPTIONS_SCALE_UP = 0x1;
	/**
	 * Constant used to indicate we should recycle the input in
	 * {@link #extractThumbnail(Bitmap, int, int, int)} unless the output is the
	 * input.
	 */
	public static final int OPTIONS_RECYCLE_INPUT = 0x2;
	/**
	 * Constant used to indicate the dimension of mini thumbnail.
	 * 
	 * @hide Only used by media framework and media provider internally.
	 */
	public static final int TARGET_SIZE_MINI_THUMBNAIL = 320;
	/**
	 * Constant used to indicate the dimension of micro thumbnail.
	 * 
	 * @hide Only used by media framework and media provider internally.
	 */
	public static final int TARGET_SIZE_MICRO_THUMBNAIL = 60;
	
	public static final int MODE_GET_METADATA_ONLY  = 0x01;
	public static final int MODE_CAPTURE_FRAME_ONLY = 0x02;

	/**
	 * Written by Swandle
	 */
	public static Drawable getThumbOrApkIcon(Context ctx, String filePath) {
		filePath = filePath.replaceAll("/*$", "");
		if (FileOperator.isApkPackage(filePath))
			return getLocalApkIcon(ctx, filePath);
		else if (FileOperator.isImageFile(filePath)) {
			Bitmap bmp = createImageThumbnail(ctx, filePath,
					Images.Thumbnails.MICRO_KIND);
			if (bmp != null) {
				return new BitmapDrawable(bmp);
			}
		} else if (FileOperator.isVideoFile(filePath)
				|| FileOperator.isAudioFile(filePath)) {
			
			if (filePath.startsWith("smb://"))
				return null;

			Bitmap bmp = /*ThumbMedia.getThumbManager().*/getMediaThumb(filePath,
					Images.Thumbnails.MICRO_KIND);
			if (bmp != null) {
				return new BitmapDrawable(bmp);
			}
		}
		return null;
	}

	/**
	 * This method first examines if the thumbnail embedded in EXIF is bigger
	 * than our target size. If not, then it'll create a thumbnail from original
	 * image. Due to efficiency consideration, we want to let MediaThumbRequest
	 * avoid calling this method twice for both kinds, so it only requests for
	 * MICRO_KIND and set saveImage to true.
	 * 
	 * This method always returns a "square thumbnail" for MICRO_KIND thumbnail.
	 * 
	 * @param filePath
	 *            the path of image file
	 * @param kind
	 *            could be MINI_KIND or MICRO_KIND
	 * @return Bitmap
	 * 
	 * @hide This method is only used by media framework and media provider
	 *       internally.
	 */
	public static Bitmap createImageThumbnail(Context ctx, String filePath,
			int kind) {
		boolean wantMini = (kind == Images.Thumbnails.MINI_KIND);
		int targetSize = wantMini ? TARGET_SIZE_MINI_THUMBNAIL
				: TARGET_SIZE_MICRO_THUMBNAIL;
		int maxPixels = wantMini ? MAX_NUM_PIXELS_THUMBNAIL
				: MAX_NUM_PIXELS_MICRO_THUMBNAIL;

		Bitmap bitmap = getThumbFromSystem(ctx, filePath);

		if (bitmap == null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;

			InputStream is = new FeFile(filePath).getInputStream();
			BitmapFactory.decodeStream(is, null, options);

			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return null;
			}
			options.inSampleSize = computeSampleSize(options, targetSize,
					maxPixels);
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			is = new FeFile(filePath).getInputStream();
			bitmap = BitmapFactory.decodeStream(is, null, options);
		}
		if (kind == Images.Thumbnails.MICRO_KIND) {
			// now we make it a "square thumbnail" for MICRO_KIND thumbnail
			bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL,
					TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;
	}

	public static Bitmap createImageThumbnail(Context ctx, Uri uri, int kind) {
		boolean wantMini = (kind == Images.Thumbnails.MINI_KIND);
		int targetSize = wantMini ? TARGET_SIZE_MINI_THUMBNAIL
				: TARGET_SIZE_MICRO_THUMBNAIL;
		int maxPixels = wantMini ? MAX_NUM_PIXELS_THUMBNAIL
				: MAX_NUM_PIXELS_MICRO_THUMBNAIL;

		ParcelFileDescriptor pfd = null;
		try {
			if (uri.getScheme().equals("file")) {
				String path = uri.getPath();
				pfd = ParcelFileDescriptor.open(new File(path),
						ParcelFileDescriptor.MODE_READ_ONLY);
			} else {
				pfd = ctx.getContentResolver().openFileDescriptor(uri, "r");
			}
		} catch (FileNotFoundException ex) {
			return null;
		}

		Bitmap b = null;
		BitmapFactory.Options options = null;
		try {
			if (pfd == null)
				return null;
			if (options == null)
				options = new BitmapFactory.Options();

			FileDescriptor fd = pfd.getFileDescriptor();
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return null;
			}
			options.inSampleSize = computeSampleSize(options, targetSize,
					maxPixels);
			options.inJustDecodeBounds = false;

			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			b = BitmapFactory.decodeFileDescriptor(fd, null, options);

			if (kind == Images.Thumbnails.MICRO_KIND) {
				b = extractThumbnail(b, TARGET_SIZE_MICRO_THUMBNAIL,
						TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
			}

		} catch (OutOfMemoryError ex) {
			Log.e(TAG, "Got oom exception ", ex);
			return null;
		} finally {
			try {
				pfd.close();
			} catch (IOException e) {
				Log.d(TAG, "", e);
			}
		}
		return b;
	}

	/**
	 * Creates a centered bitmap of the desired size.
	 * 
	 * @param source
	 *            original bitmap source
	 * @param width
	 *            targeted width
	 * @param height
	 *            targeted height
	 */
	public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
		return extractThumbnail(source, width, height, OPTIONS_NONE);
	}

	/**
	 * Creates a centered bitmap of the desired size.
	 * 
	 * @param source
	 *            original bitmap source
	 * @param width
	 *            targeted width
	 * @param height
	 *            targeted height
	 * @param options
	 *            options used during thumbnail extraction
	 */
	public static Bitmap extractThumbnail(Bitmap source, int width, int height,
			int options) {
		if (source == null) {
			return null;
		}
		float scale;
		if (source.getWidth() < source.getHeight()) {
			scale = width / (float) source.getWidth();
		} else {
			scale = height / (float) source.getHeight();
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap thumbnail = transform(matrix, source, width, height,
				OPTIONS_SCALE_UP | options);
		return thumbnail;
	}

	public static boolean saveBitmapToFile(Bitmap source,
			Bitmap.CompressFormat format, String pathName) {
		if (source == null)
			return false;

		ByteArrayOutputStream miniOutStream = new ByteArrayOutputStream();
		source.compress(format, 75, miniOutStream);
		byte[] data = null;
		try {
			miniOutStream.close();
			data = miniOutStream.toByteArray();
		} catch (java.io.IOException ex) {
			Log.e(TAG, "got exception ex " + ex);
			return false;
		}

		if (data == null)
			return false;

		try {
			RandomAccessFile r = new RandomAccessFile(pathName, "rwd");
			r.write(data);
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			Log.e(TAG, "" + e);
			return false;
		}

		return true;
	}

	/*
	 * Compute the sample size as a function of minSideLength and
	 * maxNumOfPixels. minSideLength is used to specify that minimal width or
	 * height of a bitmap. maxNumOfPixels is used to specify the maximal size in
	 * pixels that is tolerable in terms of memory usage.
	 * 
	 * The function returns a sample size based on the constraints. Both size
	 * and minSideLength can be passed in as IImage.UNCONSTRAINED, which
	 * indicates no care of the corresponding constraint. The functions prefers
	 * returning a sample size that generates a smaller bitmap, unless
	 * minSideLength = IImage.UNCONSTRAINED.
	 * 
	 * Also, the function rounds up the sample size to a power of 2 or multiple
	 * of 8 because BitmapFactory only honors sample size this way. For example,
	 * BitmapFactory downsamples an image by 2 even though the request is 3. So
	 * we round up the sample size to avoid OOM.
	 */
	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
				.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
				.min(Math.floor(w / minSideLength),
						Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == UNCONSTRAINED)
				&& (minSideLength == UNCONSTRAINED)) {
			return 1;
		} else if (minSideLength == UNCONSTRAINED) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * Transform source Bitmap to targeted width and height.
	 */
	private static Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, int options) {
		boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
		boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			/*
			 * In this case the bitmap is smaller, at least in one dimension,
			 * than the target. Transform it by placing as much of the image as
			 * possible into the target and leaving the top/bottom or left/right
			 * (or both) black.
			 */
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);
			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
					- dstY);
			c.drawBitmap(source, src, dst, null);
			if (recycle) {
				source.recycle();
			}
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();
		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;
		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}
		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
					source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}
		if (recycle && b1 != source) {
			source.recycle();
		}
		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);
		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
				targetHeight);
		if (b2 != b1) {
			if (recycle || b1 != source) {
				b1.recycle();
			}
		}
		return b2;
	}

	/**
	 * Written by Swandle
	 */
	private static Bitmap getThumbFromSystem(Context ctx, String filePath) {
		try {
			String[] projection = { MediaStore.Images.Media._ID,
					MediaStore.Images.Media.DATA };

			Cursor cursor = android.provider.MediaStore.Images.Media.query(
					ctx.getContentResolver(),
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
					MediaStore.Images.Media.DATA + "=?",
					new String[] { filePath }, MediaStore.Images.Media._ID);

			if (cursor == null || cursor.getCount() <= 0)
				return null;

			cursor.moveToFirst();
			int imgId = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Images.Media._ID));

			cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
					ctx.getContentResolver(), imgId,
					Images.Thumbnails.MINI_KIND, null);

			if (cursor == null || cursor.getCount() <= 0)
				return null;

			cursor.moveToFirst();
			int thumbId = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Images.Thumbnails._ID));

			Uri thumbUri = Uri.withAppendedPath(
					MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, ""
							+ thumbId);

			Bitmap bitmap = MediaStore.Images.Media.getBitmap(
					ctx.getContentResolver(), thumbUri);

			return bitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (IllegalStateException e) {
			Log.d(TAG, "Exception", e);
		}

		return null;
	}

	/**
	 * Moved from FileAdapter::getApkIcon(String apk)
	 */
	private static Drawable getLocalApkIcon(Context ctx, String apkPath) {
		PackageManager pm = ctx.getPackageManager();
		Drawable icon = null;

		try {
			PackageInfo pi = pm.getPackageArchiveInfo(apkPath,
					PackageManager.GET_ACTIVITIES);
			if (SysInfo.getSDKVersion() >= 8) {
				pi.applicationInfo.sourceDir = apkPath;
				pi.applicationInfo.publicSourceDir = apkPath;
				icon = pi.applicationInfo.loadIcon(pm);
			} else {
				icon = pm.getApplicationIcon(pi.applicationInfo);
			}
		} catch (Exception e) {
			return pm.getDefaultActivityIcon();
		}

		return icon;
	}
	
	private static Bitmap getMediaThumb(String filePath, int kind) {
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
			
			Method setDataSource = hideClass.getMethod("setDataSource", String.class);
			Method captureFrame = hideClass.getMethod("captureFrame");			
			Method extractAlbumArt = hideClass.getMethod("extractAlbumArt");
			Method release = hideClass.getMethod("release");
		
			Bitmap bitmap = null;
			
			if (FileOperator.isVideoFile(filePath)) {
				setMode.invoke(o, MODE_CAPTURE_FRAME_ONLY);
				setDataSource.invoke(o, filePath);
				bitmap = (Bitmap) captureFrame.invoke(o);
			} else if (FileOperator.isAudioFile(filePath)) {
				setMode.invoke(o, MODE_GET_METADATA_ONLY);
				setDataSource.invoke(o, filePath);
				byte[] art = (byte[]) extractAlbumArt.invoke(o);
				if (art != null)
					bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
			}
			
			release.invoke(o);
			
			if (kind == Images.Thumbnails.MICRO_KIND) {
				// now we make it a "square thumbnail" for MICRO_KIND thumbnail
				bitmap = extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL,
						TARGET_SIZE_MICRO_THUMBNAIL, OPTIONS_RECYCLE_INPUT);
			}
			
			return bitmap;

		} catch (ClassNotFoundException e) {
			Log.d(TAG, "Exception", e);
		} catch (Exception e) {
			Log.d(TAG, "Exception", e);
		}

		return null;
	}
}
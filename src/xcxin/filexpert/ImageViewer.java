package xcxin.filexpert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mobclick.android.MobclickAgent;

import xcxin.filexpert.Thumbnails.ThumbGetter;
import xcxin.filexpert.Thumbnails.ThumbnailUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class ImageViewer extends Activity implements
		AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory,
		View.OnClickListener, View.OnTouchListener {

	public final static String LOG_TAG = "ImageViewer";

	private Gallery mGallery = null;
	private String mFileName = null;

	private ThumbGetterListener mThumbGetter;
	private List<String> mImgList;
	private HideDelayHandler mHandler;
	private Uri mUri;
	private Bitmap mCurBitmap;

	private int mCurPos = 0;
	private int mPreTouchX = -1;
	private int mCurUserAct = 0;

	private boolean mSingleMode = false;

	private final static int HIDE_DELAY = 1;
	private final static int DELAY_TIME = 5000;

	private final static int MENU_SHARE = 1;

	public final static String FILE_NAME = "filename";

	private FileExpertSettings mSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.image_viewer);
		mSettings = new FileExpertSettings(this);

		try {
			mFileName = getIntent().getExtras().getString(FILE_NAME);
		} catch (Exception e) {
			mFileName = null;
		}
		if (mFileName == null) {
			try {
				mUri = getIntent().getData();
				if (mUri.getScheme().equals("file")) {
					mFileName = mUri.getPath();
					mSingleMode = false;
				} else
					mSingleMode = true;
			} catch (Exception e) {
				// No correct date format pass in
				return;
			}
		}

		mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
		mSwitcher.setFactory(this);
		mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));
		mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));
		mSwitcher.setOnClickListener(this);
		mSwitcher.setOnTouchListener(this);

		mGallery = (Gallery) findViewById(R.id.gallery);
		mThumbGetter = new ThumbGetterListener(this);

		if (mSingleMode) {
			mCurBitmap = ThumbnailUtils.createImageThumbnail(this, mUri,
					Images.Thumbnails.MINI_KIND);
			if (mCurBitmap != null) {
				mSwitcher.setImageDrawable(new BitmapDrawable(mCurBitmap));
			}
			mGallery.setVisibility(View.GONE);

		} else {

			mImgList = getImageFileList(mFileName);

			if (mImgList == null) {
				finish();
				return;
			}

			ThumbGetter.stopAll();

			mHandler = new HideDelayHandler();

			mGallery.setAdapter(new ImageAdapter(this, mThumbGetter, mImgList));
			mGallery.setSelection(mImgList.indexOf(mFileName));
			mGallery.setOnItemSelectedListener(this);
			mGallery.setCallbackDuringFling(false);
		}

		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "Image View");
		}

	}

	public void onItemSelected(AdapterView<?> parent, View v, int position,
			long id) {
		if (v == null)
			return;
		mCurPos = position;
		hideGalleryDelay();
		mCurBitmap = ThumbnailUtils.createImageThumbnail(this,
				(String) v.getTag(), Images.Thumbnails.MINI_KIND);
		if (mCurBitmap != null)
			mSwitcher.setImageDrawable(new BitmapDrawable(mCurBitmap));
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onClick(View v) {
		if (mGallery == null)
			return;

		if (mGallery.getVisibility() == View.INVISIBLE)
			mGallery.setVisibility(View.VISIBLE);
		else if (mGallery.getVisibility() == View.VISIBLE) {
			hideGalleryDelay();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mImgList == null)
			return false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mPreTouchX = (int) event.getRawX();
		case MotionEvent.ACTION_UP:
			int x = (int) event.getRawX();
			if (x > (mPreTouchX + 50)) {
				if (mCurPos - 1 >= 0)
					mGallery.setSelection(--mCurPos);
			} else if (x < (mPreTouchX - 50)) {
				if (mCurPos + 1 < mImgList.size())
					mGallery.setSelection(++mCurPos);
			}
		}
		return false;
	}

	public View makeView() {
		ImageView i = new ImageView(this);
		i.setBackgroundColor(0xFF000000);
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		i.setLayoutParams(new ImageSwitcher.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		return i;
	}

	private ImageSwitcher mSwitcher;

	private List<String> getImageFileList(String pathName) {
		pathName = pathName.replaceFirst("/+$", "");
		pathName = pathName.substring(0, pathName.lastIndexOf("/"));
		FeFile dir = new FeFile(pathName);

		if (!dir.isDirectory())
			return null;

		FeFile[] files = dir.listFiles();

		List<String> list = new ArrayList<String>();

		for (FeFile file : files) {
			if (FileOperator
					.isImageFile(file.getPath().replaceFirst("/+$", "")))
				list.add(file.getPath());
		}

		return list;
	}

	private void hideGalleryDelay() {
		if (mHandler == null)
			return;

		Message msg = mHandler.obtainMessage(HIDE_DELAY);
		msg.obj = ++mCurUserAct;
		mHandler.sendMessageDelayed(msg, DELAY_TIME);
	}

	private void startShareMediaActivity(String pathName) {
		String suffix = pathName.replaceAll("/+$", "")
				.substring(pathName.lastIndexOf(".")).toLowerCase();

		boolean delTmpFile = false;

		if (pathName.startsWith("smb://")) {
			pathName = FeUtil.getTempDirName();
			pathName = pathName + File.separator + "tmp." + suffix;

			Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
			if ("png".equals(suffix))
				format = Bitmap.CompressFormat.PNG;

			if (!ThumbnailUtils.saveBitmapToFile(mCurBitmap, format, pathName))
				return;

			delTmpFile = true;
		}

		FeUtil.startShareMediaActivity(pathName, this, delTmpFile);
	}

	public void finish() {
		if (mThumbGetter != null)
			mThumbGetter.stop();
		super.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_SHARE, 0, getString(R.string.share));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SHARE:
			if (mImgList != null)
				startShareMediaActivity(mImgList.get(mCurPos));
			return true;
		}
		return false;
	}

	private class HideDelayHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_DELAY:
				if (mCurUserAct == (Integer) msg.obj) {
					if (mGallery.getVisibility() == View.VISIBLE)
						mGallery.setVisibility(View.INVISIBLE);
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private class ImageAdapter extends BaseAdapter {

		public ImageAdapter(Context c, ThumbGetter thumbGetter,
				List<String> imgList) {
			this.mContext = c;
			this.mThumbGetter = thumbGetter;
			this.mImgList = imgList;
		}

		public int getCount() {
			return mImgList.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i;

			if (convertView == null) {
				i = new ImageView(mContext);
			} else {
				i = (ImageView) convertView;
			}

			Drawable d = this.mThumbGetter.getThumb(mImgList.get(position));
			if (d == null) {
				i.setImageResource(R.drawable.image);
				this.mThumbGetter.start(mImgList.get(position), false);
			} else
				i.setImageDrawable(d);

			i.setAdjustViewBounds(true);
			i.setLayoutParams(new Gallery.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			i.setTag(mImgList.get(position));
			// i.setBackgroundResource(R.drawable.picture_frame);
			return i;
		}

		private Context mContext;
		private ThumbGetter mThumbGetter;
		private List<String> mImgList;

	}

	public class ThumbGetterListener extends ThumbGetter {

		public ThumbGetterListener(Context ctx) {
			super(ctx);
		}

		@Override
		public void onThumbDone(String path, Drawable thumb) {
			if (mGallery != null) {
				ImageView i = (ImageView) mGallery.findViewWithTag(path);
				if (i != null)
					i.setImageDrawable(thumb);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onResume(this);
		}
	}
}

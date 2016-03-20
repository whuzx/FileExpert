package xcxin.filexpert.RemotePlayer;

import java.io.IOException;

import xcxin.filexpert.FeUtil;
import xcxin.filexpert.R;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RemoteAudioPlayer extends Activity implements
		View.OnClickListener, OnCompletionListener, OnErrorListener,
		OnPreparedListener {

	private Uri src_uri;
	private MediaPlayer mp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.audio_player);
		Button play_btn = (Button) this.findViewById(R.id.audio_play);
		play_btn.setOnClickListener(this);
		try {
			src_uri = getIntent().getData();
		} catch (Exception e) {
			finish();
		}
		mp = MediaPlayer.create(this, src_uri);
	}

	@Override
	public void onClick(View v) {
		mp.reset();
		try {
			mp.setDataSource(this, src_uri);
		} catch (Exception e) {
			mp.release();
			FeUtil.showToast(this, "Can not play: " + src_uri.getPath());
			return;
		}
		mp.setOnCompletionListener(this);
		mp.setOnErrorListener(this);
		mp.setOnPreparedListener(this);
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mp.prepareAsync();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		FeUtil.showToast(this, "Finished: " + src_uri.toString());
		mp.release();
		finish();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		FeUtil.showToast(this, "Error occured: " + "what: " + what
				+ ", extra: " + extra);
		mp.release();
		finish();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
	}
}
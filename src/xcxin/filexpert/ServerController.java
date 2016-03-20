package xcxin.filexpert;

import org.swiftp.FTPServerService;

import xcxin.filexpert.WebServer.WebServerService;
import android.content.Intent;
import android.widget.ImageButton;

import com.mobclick.android.MobclickAgent;

public class ServerController {

	private FileLister mLister;
	private Intent m_WebServerIntent = null;
	private Intent m_swiFtpServerIntent = null;

	public ServerController(FileLister lister) {
		mLister = lister;
	}

	private void shutdownServer(Intent i) {
		if (i != null) {
			mLister.stopService(i);
		}
		if (mLister.mWakeLock.isHeld()) {
			mLister.mWakeLock.release();
		}
		if (mLister.mWifiLock.isHeld()) {
			mLister.mWifiLock.release();
		}
	}

	public void shutdownHttpServer(boolean info) {
		shutdownServer(m_WebServerIntent);
		m_WebServerIntent = null;
		updateSharingButtonImg();
		if (info) {
			FeUtil.showToast(mLister,
					mLister.getString(R.string.share_close_ok));
		}
		if (mLister.mNm != null && mLister.mSettings.isShowNotifyIcon()) {
			mLister.mNm.showHttpNotify(mLister, false);
		}
	}

	public void shutdownFtpServer(boolean info) {
		if (m_swiFtpServerIntent != null) {
			mLister.stopService(m_swiFtpServerIntent);
			m_swiFtpServerIntent = null;
		}
		updateSharingButtonImg();
		if (info) {
			FeUtil.showToast(mLister,
					mLister.getString(R.string.share_close_ok));
		}
		if (mLister.mNm != null && mLister.mSettings.isShowNotifyIcon()) {
			mLister.mNm.showFtpNotify(mLister, false);
		}
	}

	public boolean startHttpSharing(String rootDir) {
		shutdownHttpServer(false);
		if (m_WebServerIntent == null) {
			m_WebServerIntent = new Intent();
			m_WebServerIntent.setClass(mLister, WebServerService.class);
		}
		mLister.mSettings.setSharingDir(rootDir);
		mLister.startService(m_WebServerIntent);
		if (mLister.mSettings.isWebLoginEnable() == false) {
			mLister.showInfo(mLister.getString(R.string.share_success)
					+ "http://" + FileLister.getLocalIpAddress() + ":"
					+ mLister.mSettings.getHttpPort() + "/",
					mLister.getString(R.string.network_share), false);
		} else {
			mLister.showInfo(mLister.getString(R.string.share_success)
					+ "http://" + FileLister.getLocalIpAddress() + ":"
					+ mLister.mSettings.getHttpPort() + "/" + "\n" +
					mLister.getString(R.string.web_user) + " " + mLister.mSettings.getWebUsername() + "\n" +
					mLister.getString(R.string.web_password) + " " + mLister.mSettings.getWebPassword(),
					mLister.getString(R.string.network_share), false);
		}
		if (mLister.mSettings.getHttpWakelock() == true) {
			mLister.mWakeLock.acquire();
			mLister.mWakeLock.setReferenceCounted(false);
			FeUtil.showToast(mLister,
					mLister.getString(R.string.share_power_warning));
		}
		updateSharingButtonImg();

		if (mLister.mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(mLister, "http");
		}

		mLister.ActiviteNetwork();

		if (mLister.mNm != null && mLister.mSettings.isShowNotifyIcon()) {
			mLister.mNm.showHttpNotify(mLister, true);
		}
		return true;
	}

	public boolean startFtpSharing(String rootDir) {
		shutdownFtpServer(false);
		if (startSwiFtpSharing(rootDir) == false) {
			return false;
		}
		mLister.showInfo(
				mLister.getString(R.string.ftp_success) + "\n"
						+ mLister.getString(R.string.ftp_addr)
						+ FileLister.getLocalIpAddress() + "\n"
						+ mLister.getString(R.string.ftp_user)
						+ mLister.mSettings.getFtpUserName() + "\n"
						+ mLister.getString(R.string.ftp_pass)
						+ mLister.mSettings.getFtpPassword() + "\n"
						+ mLister.getString(R.string.ftp_port)
						+ mLister.mSettings.getFtpPort(),
				mLister.getString(R.string.network_share), false);
		updateSharingButtonImg();
		if (mLister.mSettings.getFtpWakelock() == true) {
			mLister.mWakeLock.acquire();
			mLister.mWakeLock.setReferenceCounted(false);
			FeUtil.showToast(mLister,
					mLister.getString(R.string.share_power_warning));
		}

		if (mLister.mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(mLister, "ftp");
		}

		mLister.ActiviteNetwork();
		if (mLister.mNm != null && mLister.mSettings.isShowNotifyIcon()) {
			mLister.mNm.showFtpNotify(mLister, true);
		}
		return true;
	}

	protected boolean startSwiFtpSharing(String rootDir) {
		if (m_swiFtpServerIntent != null) {
			mLister.stopService(m_swiFtpServerIntent);
			m_swiFtpServerIntent = null;
		}
		m_swiFtpServerIntent = new Intent();
		m_swiFtpServerIntent.setClass(mLister, FTPServerService.class);
		m_swiFtpServerIntent.putExtra("rootdir", rootDir);
		mLister.startService(m_swiFtpServerIntent);
		return true;
	}

	protected void updateSharingButtonImg() {
		ImageButton ib = (ImageButton) mLister.findViewById(R.id.btn_http);
		if (ib != null)
			if (m_WebServerIntent != null) {
				ib.setImageDrawable(mLister.getResources().getDrawable(
						R.drawable.http_off));
			} else {
				ib.setImageDrawable(mLister.getResources().getDrawable(
						R.drawable.http_on));
			}
		ib = (ImageButton) mLister.findViewById(R.id.btn_ftp);
		if (ib != null)
			if (m_swiFtpServerIntent != null) {
				ib.setImageDrawable(mLister.getResources().getDrawable(
						R.drawable.ftp_off));
			} else {
				ib.setImageDrawable(mLister.getResources().getDrawable(
						R.drawable.ftp_on));
			}
	}

	public boolean isServerStarted() {
		if (m_WebServerIntent != null || m_swiFtpServerIntent != null)
			return true;
		return false;
	}

	public boolean isHttpServerStarted() {
		if (m_WebServerIntent != null)
			return true;
		return false;
	}

	public boolean isFtpServerStarted() {
		if (m_swiFtpServerIntent != null)
			return true;
		return false;
	}
}

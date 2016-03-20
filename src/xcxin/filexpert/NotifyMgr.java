package xcxin.filexpert;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotifyMgr {

	private NotificationManager nm;

	private int HTTP_NTF = 1;
	private int FTP_NTF = 2;
	private int FE_NTF = 3;
	
	public static final String HTTP_KEY = "FE_HTTP";
	
	public NotifyMgr(Context context) {
		nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void showHttpNotify(Context context, boolean enable) {
		if (enable) {
			int icon = R.drawable.http_on;
			CharSequence tickerText = context.getString(R.string.http_sharing_on);
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);
			CharSequence contentTitle = context.getString(R.string.http_sharing_on);
			CharSequence contentText = context.getString(R.string.touch_cfg);
			Intent notificationIntent = new Intent(context, FileLister.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			nm.notify(HTTP_NTF, notification);
		} else {
			nm.cancel(HTTP_NTF);
		}
	}

	public void showFtpNotify(Context context, boolean enable) {
		if (enable) {
			int icon = R.drawable.ftp_on;
			CharSequence tickerText = context.getString(R.string.ftp_sharing_on);
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);
			CharSequence contentTitle = context.getString(R.string.ftp_sharing_on);
			CharSequence contentText = context.getString(R.string.touch_cfg);
			Intent notificationIntent = new Intent(context, FileLister.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			nm.notify(FTP_NTF, notification);
		} else {
			nm.cancel(FTP_NTF);
		}
	}

	public void showFeNotify(Context context, boolean enable) {
		if (enable) {
			int icon = R.drawable.icon;
			CharSequence tickerText = context.getString(R.string.fe_running);
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);
			CharSequence contentTitle = context.getString(R.string.fe_running);
			CharSequence contentText = context.getString(R.string.fe_touch_switch);
			Intent notificationIntent = new Intent(context, FileLister.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			nm.notify(FE_NTF, notification);
		} else {
			nm.cancel(FE_NTF);
		}
	}

	public void showBackgroudTaskNotify(Context context, boolean enable) {
		if (enable) {
			int icon = R.drawable.icon;
			CharSequence tickerText = context.getString(R.string.fe_running);
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);
			CharSequence contentTitle = "Backgroud Task Running";
			CharSequence contentText = "Touch to show task batch dialog to operate";
			Intent notificationIntent = new Intent(context.getString(R.string.action_name));
			PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			nm.notify(FE_NTF, notification);
		} else {
			nm.cancel(FE_NTF);
		}
	}
}

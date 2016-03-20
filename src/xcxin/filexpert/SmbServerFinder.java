package xcxin.filexpert;

import java.util.concurrent.atomic.AtomicInteger;

import jcifs.smb.SmbFile;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SmbServerFinder extends Thread implements Handler.Callback {

	private String phone_ip;
	private ProgressDialog m_pd = null;
	private Context ctx;
	private AtomicInteger ui_sync;
	private SmbServerMgr smbMgr;
	private Handler handler;
	private Bundle bdl;

	public SmbServerFinder(Context context, SmbServerMgr smbMgr) {
		phone_ip = FileLister.getLocalIpAddress();
		ctx = context;
		this.smbMgr = smbMgr;
		ui_sync = new AtomicInteger();
		handler = new Handler(this);
		bdl = new Bundle();
	}

	@Override
	public void run() {
		super.run();
		
		// Get my ip base
		String ip_base = phone_ip.substring(0, phone_ip.lastIndexOf("."));
		// For each ip, try
		for (int addr = 1; addr < 255; addr++) {
			String ip = ip_base + "." + Integer.toString(addr);
			if (ip.compareTo(phone_ip) != 0) {
				// Does this IP already added?
				if (smbMgr.isAdded(ip) == false) {
					sendMsgAndWait("update_msg", ip);
					SmbFile sf;
					try {
						sf = new SmbFile("smb://" + ip + "/");
						String[] files = sf.list();
						if (files != null) {
							if (files.length > 0) {
								// Success! We've found one
								smbMgr.add("", ip, true, "", "");
								files = null;
							}
						}
					} catch (Exception e) {
						;
					}
					sf = null;
				}
			}
		}
		
		sendMsgAndWait("exit_msg", "exit");
		System.gc();
	}

	@Override
	public synchronized void start() {
		m_pd = ProgressDialog.show(ctx, ctx.getString(R.string.search_smb_svr), "");
		super.start();
	}

	@Override
	public boolean handleMessage(Message msg) {
		String dlg_msg = bdl.getString("update_msg");
		if (dlg_msg != null) {
			m_pd.setMessage(ctx.getString(R.string.deal_with) + " " + dlg_msg);
		}
		dlg_msg = bdl.getString("exit_msg");
		if (dlg_msg != null) {
			m_pd.dismiss();
			((FileLister) ctx).refresh();
		}
		ui_sync.set(1);
		return true;
	}

	private void sendMsgAndWait(String key, String key_msg) {
		Message msg = handler.obtainMessage();
		bdl.clear();
		bdl.putString(key, key_msg);
		msg.setData(bdl);
		handler.sendMessage(msg);
		ui_sync.set(0);
		//while (ui_sync.get() != 1)
		//	;
	}

}

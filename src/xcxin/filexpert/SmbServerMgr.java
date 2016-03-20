package xcxin.filexpert;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class SmbServerMgr {

	public class smbServer {
		public String mIp;
		public String mUser;
		public String mPassword;
		public String mDomain;
		public boolean mAny;
	}

	SharedPreferences mSettings;
	SharedPreferences.Editor mEditor;
	private int mCount;
	private List<smbServer> mSmbServers = new ArrayList<smbServer>();

	private final String SETTINGS_NAME = "FE_SMB_SERVER";
	private final String SMB_SERVER_COUNT = "NumberOfSmbServers";

	public SmbServerMgr(Context act) {
		mSettings = act.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		mCount = mSettings.getInt(SMB_SERVER_COUNT, 0);
		mSmbServers.clear();
		mEditor = mSettings.edit();
		if (mCount != 0) {
			for (int index = 0; index < mCount; index++) {
				smbServer server = new smbServer();
				server.mIp = mSettings.getString("IP" + index, null);
				server.mUser = mSettings.getString("USER" + index, null);
				server.mPassword = mSettings
						.getString("PASSWORD" + index, null);
				server.mAny = mSettings.getBoolean("mAny" + index, false);
				server.mDomain = mSettings.getString("DOMAIN" + index, null);
				mSmbServers.add(server);
				mEditor.remove("KEY" + index);
				mEditor.remove("PATH" + index);
			}
		}
	}

	public void SmbServerUpdateCache() {
		commitSmbServers();
		mCount = mSettings.getInt(SMB_SERVER_COUNT, 0);
		mSmbServers.clear();
		mEditor = mSettings.edit();
		if (mCount != 0) {
			for (int index = 0; index < mCount; index++) {
				smbServer server = new smbServer();
				server.mIp = mSettings.getString("IP" + index, null);
				server.mUser = mSettings.getString("USER" + index, null);
				server.mPassword = mSettings
						.getString("PASSWORD" + index, null);
				server.mAny = mSettings.getBoolean("mAny" + index, false);
				server.mDomain = mSettings.getString("DOMAIN" + index, null);
				mSmbServers.add(server);
				mEditor.remove("KEY" + index);
				mEditor.remove("PATH" + index);
			}
		}
	}

	public void commitSmbServers() {
		smbServer server = null;
		for (int index = 0; index < mSmbServers.size(); index++) {
			server = mSmbServers.get(index);
			mEditor.putString("IP" + index, server.mIp);
			mEditor.commit();
			mEditor.putString("USER" + index, server.mUser);
			mEditor.commit();
			mEditor.putString("PASSWORD" + index, server.mPassword);
			mEditor.commit();
			mEditor.putString("DOMAIN" + index, server.mDomain);
			mEditor.commit();
			mEditor.putBoolean("mAny" + index, server.mAny);
			mEditor.commit();
		}
		mEditor.putInt(SMB_SERVER_COUNT, mSmbServers.size());
		mEditor.commit();
	}

	public List<String> list() {
		List<String> server_list = new ArrayList<String>();
		server_list.clear();
		for (int index = 0; index < mSmbServers.size(); index++) {
			server_list.add(mSmbServers.get(index).mIp);
		}
		return server_list;
	}

	public boolean isAdded(String Ip) {
		smbServer server;
		for (int index = 0; index < mSmbServers.size(); index++) {
			server = mSmbServers.get(index);
			if (Ip.compareTo(server.mIp) == 0) {
				return true;
			}
		}
		return false;
	}

	public String getIp(int id) {
		if (id > mSmbServers.size()) {
			return null;
		}
		return mSmbServers.get(id).mIp;
	}

	public String getDomain(int id) {
		if (id > mSmbServers.size()) {
			return null;
		}
		return mSmbServers.get(id).mDomain;
	}
	
	public String getUserName(int id) {
		if (id > mSmbServers.size()) {
			return null;
		}
		return mSmbServers.get(id).mUser;
	}

	public String getPassword(int id) {
		if (id > mSmbServers.size()) {
			return null;
		}
		return mSmbServers.get(id).mPassword;
	}

	public boolean isAllowAny(int id) {
		if (id > mSmbServers.size()) {
			return false;
		}
		return mSmbServers.get(id).mAny;
	}

	public smbServer getServer(int id) {
		if (id > mSmbServers.size()) {
			return null;
		}
		return mSmbServers.get(id);
	}

	public void add(String domain, String Ip, boolean Any, String User, String Password) {
		smbServer server = new smbServer();
		server.mIp = Ip;
		server.mUser = User;
		server.mPassword = Password;
		server.mAny = Any;
		server.mDomain = domain;
		mSmbServers.add(server);
	}

	public void modify(String OldIp, String Domain, String Ip, boolean Any, String User,
			String Password) {
		smbServer server;
		for (int index = 0; index < mSmbServers.size(); index++) {
			server = mSmbServers.get(index);
			if (server.mIp.compareTo(OldIp) == 0) {
				server.mIp = Ip;
				server.mUser = User;
				server.mPassword = Password;
				server.mDomain = Domain;
				server.mAny = Any;
				return;
			}
		}
	}

	public void remove(int id) {
		if (id > mSmbServers.size()) {
			return;
		}
		mSmbServers.remove(id);
	}

	public int getCount() {
		return mSmbServers.size();
	}
}
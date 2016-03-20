package xcxin.filexpert.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xcxin.filexpert.FeUtil;
import xcxin.filexpert.WebServer.FeServletBase;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;

public class sms extends FeServletBase {

	private JSONObject smsJson;
	private String mResult;
	private String[] PROJECTION = { "address", "date", "read", "body", "person" };

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
		if (smsJson != null && mResult != null) {
			writer.write(mResult);
		} else {
			writer.write("Server internal error");
		}
		writer.flush();
	}

	@Override
	public boolean isContentTypeSet() {
		return false;
	}

	@Override
	public void execute() throws Exception {
		super.execute();
		smsJson = getJsonFromPostData();
		if (smsJson != null) {
			boolean result = executeJsonCommand(smsJson);
			if (!result) {
				throw new Exception("Undefined sms command received: "
						+ smsJson.get("type"));
			}
		}
	}

	public static boolean sendSMS(Context context, String number, String text) {
		try {
			PendingIntent pi = PendingIntent.getBroadcast(context, 0,
					new Intent(), 0);
			SmsManager.getDefault().sendTextMessage(number, null, text, pi,
					null);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public Cursor getAllUnreadSms(long after) {
		String selection = "read = '0'" + " AND " + "date > '"
				+ String.valueOf(after) + "'";
		Cursor c = getService().getContentResolver().query(
				Uri.parse("content://sms/inbox"), PROJECTION, selection, null,
				"date");
		return c;
	}

	private String generateSendResult(boolean result, String number)
			throws JSONException {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		json1.put("number", number);
		json1.put("result", result);
		json.put("name", "sms");
		json.put("type", "result");
		json.put("data", json1);
		return json.toString();
	}

	private JSONObject generateUnreadJson(JSONObject received)
			throws JSONException {
		long time = received.getLong("time");
		if (time < 0) {
			switch ((int) time) {
			case -1:
				JSONObject json = new JSONObject();
				json.put("type", "getunread");
				json.put("data", null);
				json.put("name", "sms");
				String selection = "read = '0'";
				Cursor c = getService().getContentResolver().query(
						Uri.parse("content://sms/inbox"), PROJECTION,
						selection, null, "date");
				if (c == null || c.getCount() == 0) {
					// There's no unread sms in the inbox
					json.put("time", -2);
				} else {
					c.moveToLast();
					int index = c.getColumnIndex("date");
					json.put("time", c.getLong(index));
				}
				c.close();
				return json;
			case -2:
				return convertCursor2Json(getAllUnreadSms(0), 0);
			}
		}
		Cursor c = getAllUnreadSms(time);
		return convertCursor2Json(c, time);
	}

	private boolean executeJsonCommand(JSONObject json) throws JSONException {
		if (json.get("type").toString().equals("send")) {
			String number = getNumberFromTopJson(json);
			String body = getBodyFromTopJson(json);
			boolean r = sendSMS(getService(), number, body);
			mResult = generateSendResult(r, getNumberFromTopJson(json));
			return r;
		} else if (json.get("type").toString().equals("getunread")) {
			smsJson = generateUnreadJson(json);
			mResult = smsJson.toString();
			return true;
		}
		return false;
	}

	private String getNumberFromTopJson(JSONObject json) throws JSONException {
		JSONObject person = (JSONObject) json.get("data");
		String number = person.getString("number");
		return number;
	}

	private String getBodyFromTopJson(JSONObject json) throws JSONException {
		JSONObject person = (JSONObject) json.get("data");
		String number = person.getString("content");
		return number;
	}

	private JSONObject convertCursor2Json(Cursor c, long serviceStartTime)
			throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", "sms");
		json.put("type", "getunread");
		if (c == null || c.getCount() == 0) {
			json.put("data", null);
			json.put("time", serviceStartTime);
			c.close();
			return json;
		}
		c.moveToFirst();
		JSONArray array = new JSONArray();
		do {
			JSONObject entry = new JSONObject();
			int index = c.getColumnIndex("address");
			entry.put("number", c.getString(index));
			index = c.getColumnIndex("body");
			entry.put("content", c.getString(index));
			index = c.getColumnIndex("person");
			int pid = c.getInt(index);
			entry.put("person", pid);
			index = c.getColumnIndex("date");
			entry.put("date", getDateString(c.getLong(index)));
			array.put(entry);
			if (c.isLast()) {
				// Update time line
				json.put("time", c.getLong(c.getColumnIndex("date")));
				break;
			}
			c.moveToNext();
		} while (true);
		c.close();
		json.put("data", array);
		return json;
	}

	private String getDateString(long date) {
		Date d = new Date(date);
		return d.toLocaleString();
	}
}

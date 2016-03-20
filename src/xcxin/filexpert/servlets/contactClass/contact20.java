package xcxin.filexpert.servlets.contactClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xcxin.filexpert.WebServer.FeServletBase;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class contact20 extends ContactBase {

	public contact20(FeServletBase servlet) {
		super(servlet);
	}

	@Override
	public String getContactDataJson() {
		Cursor c = getAllContacts();
		if (c != null) {
			JSONObject json = convert2JSON(c);
			if (json != null) {
				return json.toString();
			}
		}
		return null;
	}

	private Cursor getAllContacts() {
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME, Phone.NUMBER,
				Email.DATA, Organization.COMPANY };
		String selection = ContactsContract.Data.IN_VISIBLE_GROUP + " = '1'"
				+ " AND " + ContactsContract.Data.IS_PRIMARY + " = '1'";
		return getServlet()
				.getService()
				.getContentResolver()
				.query(ContactsContract.Data.CONTENT_URI, projection,
						selection, null, null);
	}

	private JSONObject convert2JSON(Cursor c) {

		JSONObject json = new JSONObject();
		JSONArray records = new JSONArray();

		c.moveToFirst();
		do {
			int index = c
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			String name = c.getString(index);
			int num;
			int id;
			index = c
					.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			num = c.getInt(index);
			index = c.getColumnIndex(ContactsContract.Contacts._ID);
			id = c.getInt(index);
			String email = c.getString(c.getColumnIndex(Email.DATA));
			String company = c.getString(c.getColumnIndex(Organization.COMPANY));
			JSONObject record = new JSONObject();
			try {
				record.put("name", name);
				record.put("number", num);
				record.put("email", email);
				record.put("company", company);
				record.put("id", id);
			} catch (JSONException e) {
				break;
			}
			records.put(record);
			if (c.isLast() == true) {
				break;
			} else {
				c.moveToNext();
			}
		} while (true);
		c.close();

		try {
			json.put("name", "contact");
			json.put("type", "query");
			json.put("data", records);
		} catch (JSONException e) {
			return null;
		}

		return json;
	}

	public boolean hasTelephone(Cursor c) {
		if (c.getInt(c
				.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 1)
			return true;
		return false;
	}
}

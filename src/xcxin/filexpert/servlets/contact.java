package xcxin.filexpert.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import xcxin.filexpert.SysInfo;
import xcxin.filexpert.WebServer.FeServletBase;
import xcxin.filexpert.servlets.contactClass.ContactBase;
import xcxin.filexpert.servlets.contactClass.contact16;
import xcxin.filexpert.servlets.contactClass.contact20;

public class contact extends FeServletBase {

	private String contactsData = null;

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if (contactsData != null) {
			OutputStreamWriter writer = new OutputStreamWriter(outstream,
					"UTF-8");
			writer.write(contactsData);
			writer.flush();
		} else {
			set404(true);
			send404IfNeeded(outstream);
		}
	}

	@Override
	public void execute() throws Exception {
		// TODO Auto-generated method stub
		super.execute();
		ContactBase	contactProvider = null;
		if (SysInfo.getSDKVersion() >= 5) {
			contactProvider = new contact20(this);
		} else {
			contactProvider = new contact16(this);
		}
		contactsData = contactProvider.getContactDataJson();
	}
	@Override
	public boolean isContentTypeSet() {
		return false;
	}
}

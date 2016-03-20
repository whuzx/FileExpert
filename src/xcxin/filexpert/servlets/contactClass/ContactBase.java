package xcxin.filexpert.servlets.contactClass;

import xcxin.filexpert.WebServer.FeServletBase;

public abstract class ContactBase {
	
	private FeServletBase mServlet;
	
	public ContactBase(FeServletBase servlet) {
		mServlet = servlet;
	}
	
	protected FeServletBase getServlet() {
		return mServlet;
	}
	
	public abstract String getContactDataJson();
}

package xcxin.filexpert.servlets.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import xcxin.filexpert.DirTreeHelper;
import xcxin.filexpert.FeComparator;
import xcxin.filexpert.FeUtil;
import xcxin.filexpert.FileExpertSettings;
import xcxin.filexpert.FileOperator;
import xcxin.filexpert.QuickSort;
import xcxin.filexpert.R;
import xcxin.filexpert.SortComparator;
import android.app.Service;

public class DirectoryHtmlGenerator {

	OutputStreamWriter mWriter;
	File mDir;
	Service mService;
	String mRootPath;
	String mPath;
	String mParentPath;
	private FileExpertSettings mSettings;

	final static String LIST_PATH = "/webapps/file/listing";
	final static String RESOURCE_PATH = "/webapps/Resources";

	public DirectoryHtmlGenerator(OutputStreamWriter writer, File dir,
			Service service, String rootPath) {
		mWriter = writer;
		mDir = dir;
		mService = service;
		mRootPath = rootPath;
		mSettings = new FileExpertSettings(mService);
		mPath = getRelativePath(mDir.getAbsolutePath(), mRootPath);
		if (mPath.equals("")) {
			mPath = "/";
		}
		mParentPath = DirTreeHelper.getPreviousDir(mPath);
	}

	public DirectoryHtmlGenerator(Service service) {
		mWriter = null;
		mDir = null;
		mService = service;
		mRootPath = null;
	}

	protected void WriteTitle(String title) throws IOException {
		mWriter.write("<title>" + title + "</title>\n");
	}

	protected void WriteHead1(String head) throws IOException {
		mWriter.write("<h1>" + head + "</h1>\n");
	}

	protected void WriteAddress(String address) throws IOException {
		mWriter.write("<address>" + address + "</address>\n");
	}

	protected void writeToolBar() throws IOException {

		mWriter.write("<div class=\"toolbar\">");
		mWriter.write("<div class=\"tools_div\">");
		mWriter.write("<a id=\"link_delete\" href=\"#\" style=\"height:99%;\">"
				+ mService.getString(R.string.delete) + "</a>\n");
		mWriter.write("</div>");
		mWriter.write("<div class=\"tools_div\">");
		mWriter.write("<a href=\"#\" onclick=\"createNewFolder('"
				+ mService.getString(R.string.input_new_folder_name)
				+ "','&createfolder=','"
				+ mService.getString(R.string.default_new_folder_name)
				+ "')\" style=\"height:99%;\">"
				+ mService.getString(R.string.create_new_folder) + "</a>");
		mWriter.write("</div>");
		mWriter.write("<div class=\"tools_div\">");
		mWriter.write("<a href=\"" + LIST_PATH + "?path=" + mParentPath
				+ "\" style=\"height:99%;\">"
				+ mService.getString(R.string.web_prev_dir) + "</a>\n");
		mWriter.write("</div>");
		mWriter.write("</div>");
	}

	protected void writeDownloadAll() throws IOException {
		String path = getRelativePath(mDir.getAbsolutePath(), mRootPath);
		mWriter.write("<a href=\"#\" onclick=\"downloadAll('downloadAll.zip?path="
				+ path
				+ "/\')\">"
				+ mService.getString(R.string.downloadall)
				+ "</a>");
		mWriter.write("</br>");
	}

	protected void WriteUploadForm() throws IOException {
		mWriter.write("<div>");
		mWriter.write("<br>");
		mWriter.write("<form enctype=\"multipart/form-data\" method=\"post\">");
		mWriter.write("<table><tr><td colspan=\"3\">");
		mWriter.write(mService.getString(R.string.web_upload) + "</td></tr>");
		mWriter.write("<tr><td>");
		mWriter.write("<input type=\"file\" name=\"datafile\" size=\"40\">");
		mWriter.write("</td><td></td><td>");
		mWriter.write("<input type=\"submit\" value=\""
				+ mService.getString(R.string.web_send)
				+ "\"></td></tr></table></form>");
		mWriter.write("<br/>");
	}

	public void Generate() throws IOException {
		mWriter.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n");
		mWriter.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		mWriter.write("<head>");
		if (mPath.length() == 0) {
			WriteTitle("/ - Powered by File Expert");
		} else {
			WriteTitle(mPath + " - Powered by File Expert");
		}
		WriteScripts();
		mWriter.write("</head>");
		mWriter.write("<body>\n<div>");
		WriteHead1(mService.getString(R.string.web_head));
		writeToolBar();
		mWriter.write("<table border=\"0\" class=\"sortable paginated\">\n");
		mWriter.write("<thead>");
		mWriter.write("<tr><th class=\"td_sel\"><input type=\"checkbox\" class=\"select_all\" /></th>"
				+ "<th class=\"sort-date\">"
				+ mService.getString(R.string.itemtype)
				+ "</th>"
				+ "<th class=\"sort-alpha\">"
				+ mService.getString(R.string.web_name)
				+ "</th><th class=\"sort-date\">"
				+ mService.getString(R.string.web_last)
				+ "</th><th class=\"sort-numeric\">"
				+ mService.getString(R.string.web_size)
				+ "</th><th>"
				+ mService.getString(R.string.operation)
				+ "</th></tr></thead>\n");
		mWriter.write("<tbody>");
		// To do Add contents mark
		WriteDirContents();
		mWriter.write("</tbody></table>\n");
		WriteUploadForm();
		// writeDownloadAll();
		WriteAddress(mService.getString(R.string.web_tail));
		mWriter.write("<br>");
		writeCopyright();
		mWriter.write("</div></div>");
		if (mSettings.isWebPageIndex()) {
			writePageIndex(25);
		} else {
			writePageIndex(0);
		}
		writeHiddenField("deleteWarn", mService.getString(R.string.delete_warn));
		writeHiddenField("deleteEmpty",
				mService.getString(R.string.delete_empty));
		mWriter.write("</body>\n</html>");
		mWriter.flush();
	}

	protected void WriteDirContents() throws IOException {
		String[] files = mDir.list();
		if (files == null || files.length == 0)
			return;
		FeComparator compareCls = new SortComparator.TypeComparator(
				mDir.getAbsolutePath());
		files = QuickSort.perform(files, 0, files.length - 1, compareCls);
		File item;
		for (int index = 0; index < files.length; index++) {
			item = new File(mDir.getAbsolutePath(), files[index]);
			String path = getRelativePath(item.getAbsolutePath(), mRootPath);
			if (item.isFile() == true) {
				mWriter.write("<tr><td class=\"td_sel\">"
						+ "<input type=\"checkbox\" sel_name=\""
						+ item.getName()
						+ "\" /></td>"
						+ "<td class=\"icon\" datenum=\""
						+ index
						+ "\"><img src=\""
						+ getImgResource(item)
						+ "\" alt=\"\" border=\"0\" width=\"24\" height=\"24\" style=\"margin:0 auto;\" /></td>"
						+ "<td style=\"width:350px;\"><a href=\"" + getDownloadFileName(path)
						+ "\">" + item.getName()
						+ "</a></td><td align=\"right\" datenum=\""
						+ item.lastModified() + "\" >" + getLastModified(item)
						+ "</td><td align=\"right\">"
						+ getFileSizeShowStr(item.length()) + "</td>");
			} else {
				mWriter.write("<tr><td class=\"td_sel\">"
						+ "<input type=\"checkbox\" sel_name=\""
						+ item.getName()
						+ "\" /></td>"
						+ "<td class=\"icon\" datenum=\""
						+ index
						+ "\"><img src=\""
						+ getImgResource(item)
						+ "\" alt=\"\" border=\"0\" width=\"24\" height=\"24\" style=\"margin:0 auto;\" /></td>"
						+ "<td style=\"width:350px;\"><a href=\"" + LIST_PATH
						+ "?path=" + path + "/" + "\">" + item.getName()
						+ "</a></td><td align=\"right\" datenum=\""
						+ item.lastModified() + "\" >" + getLastModified(item)
						+ "</td><td align=\"right\"> - </td>");
			}

			mWriter.write("<td align=\"center\">"
					+ "<a href=\"#\" onclick=\"ask4confirm('"
					+ mService.getString(R.string.delete_confirm)
					+ "', '&delete=" + item.getName() + "')\">"
					+ mService.getString(R.string.delete) + "</a>  |  "
					+ "<a href=\"#\" onclick=\"rename('&oldname="
					+ item.getName() + "&newname=','"
					+ mService.getString(R.string.input_new_name) + "','"
					+ item.getName() + "')\">"
					+ mService.getString(R.string.rename) + "</a></td></tr>\n");
		}
	}

	protected String getLastModified(File file) {
		Date date = new Date(file.lastModified());
		return date.toLocaleString();
	}

	protected String getFileSizeShowStr(long filesize) {
		if (filesize < 1024) {
			return filesize + " Bytes";
		}
		if (filesize < 1024 * 1024) {
			return (filesize / 1024) + " KB";
		}
		return (filesize / (1024 * 1024) + " MB");
	}

	protected String getRelativePath(String fullPath, String rootPath) {
		return fullPath.substring(fullPath.indexOf(rootPath)
				+ rootPath.length());
	}

	protected void WriteScripts() throws IOException {
		mWriter.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/st.css\" /><script type=\"text/javascript\" src=\"/jquery.js\"></script><script type=\"text/javascript\" src=\"/st.js\"></script>");
		mWriter.write("<script language=\"JavaScript\" type=\"text/JavaScript\">");
		InputStream is = mService.getAssets().open("scripts.txt");
		mWriter.write(FeUtil.convertStreamToString(is));
		mWriter.write("</script>");
	}

	protected void writePageIndex(int index) throws IOException {
		mWriter.write("<input type=\"hidden\" id=\"pagerIndex\" value=\""
				+ index + "\"/>");
	}

	protected void writeHiddenField(String id, String value) throws IOException {
		mWriter.write("<input type=\"hidden\" id=\"" + id + "\" value=\""
				+ value + "\"/>");
	}

	protected void writeCopyright() throws IOException {
		mWriter.write("<span>Copyright &copy; Xi'An Geek Software Technology 2011 All Rights Reserved</span>");
	}

	protected String getImgResource(File file) {
		String rootPath = RESOURCE_PATH + "?id=";
		String fileType = FileOperator.getExtendFileName(file).toUpperCase();
		if (file.isDirectory())
			return rootPath + R.drawable.folder;
		else if (fileType.equals("TXT"))
			return rootPath + R.drawable.file;
		else if (fileType.equals("APK"))
			return rootPath + R.drawable.apk_package;
		else if (fileType.equals("MP3"))
			return rootPath + R.drawable.audio;
		else if (fileType.equals("MP4"))
			return rootPath + R.drawable.video;
		else if (fileType.equals("3GP"))
			return rootPath + R.drawable.video;
		else if (fileType.equals("WMV"))
			return rootPath + R.drawable.video;
		else if (fileType.equals("RM"))
			return rootPath + R.drawable.video;
		else if (fileType.equals("RMVB"))
			return rootPath + R.drawable.video;
		else if (fileType.equals("DOC"))
			return rootPath + R.drawable.doc;
		else if (fileType.equals("XLS"))
			return rootPath + R.drawable.xls;
		else if (fileType.equals("PDF"))
			return rootPath + R.drawable.pdf_icon;
		else if (fileType.equals("PPT"))
			return rootPath + R.drawable.ppt;
		else if (fileType.equals("ZIP"))
			return rootPath + R.drawable.zip;
		else
			return rootPath + R.drawable.file;
	}
	
	private String getDownloadFileName(String origname) {
		return origname;
	}
}
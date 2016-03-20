package xcxin.filexpert.servlets;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xcxin.filexpert.FeFile;
import xcxin.filexpert.FileOperator;
import xcxin.filexpert.QuickSort;
import xcxin.filexpert.SortComparator;
import xcxin.filexpert.WebServer.FeServletBase;

public class explorer extends FeServletBase {

	private JSONObject cmdJson;
	protected JSONObject statusJson;

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
		if (statusJson != null) {
			writer.write(statusJson.toString());
		} else {
			writer.write("Server internal error");
		}
		writer.flush();
	}

	@Override
	public void execute() throws Exception {
		super.execute();
		cmdJson = getJsonFromPostData();
		if (cmdJson != null) {
			executeJsonCommand(cmdJson);
		} else {
			runList("/");
		}
	}

	@Override
	public boolean isContentTypeSet() {
		return false;
	}

	private boolean executeJsonCommand(JSONObject json) throws Exception {

		if (json == null)
			return false;

		if (json.getString("name").equals("file")) {
			String cmd = json.getString("type");
			if (cmd.equals("list")) {
				runList(json);
				return true;
			} else if (cmd.equals("copy")) {
				runCopy(json);
				return true;
			} else if (cmd.equals("delete")) {
				runDelete(json);
				return true;
			} else if (cmd.equals("rename")) {
				runRename(json);
				return true;
			} else if (cmd.equals("create")) {
				runCreate(json);
				return true;
			} else {
				throw new JSONException("Undefined json command: " + cmd);
			}
		}

		throw new JSONException("Undefined json name: "
				+ json.getString("name"));
	}

	private void runList(JSONObject json) throws JSONException {

		String file = json.getString("src");
		String attr = json.getString("attr");

		boolean showDir, showFile;
		if (attr.equals("dir")) {
			showDir = true;
			showFile = false;
		} else if (attr.equals("file")) {
			showDir = false;
			showFile = true;
		} else if (attr.equals("all")) {
			showDir = true;
			showFile = true;
		} else {
			throw new JSONException("Undefined list attr: " + attr);
		}

		FeFile srcFile = new FeFile(getRoot(), file);
		if (srcFile.isFile()) {
			generateStatusReportJson(json, "Can not list on file");
			return;
		}
		if(!srcFile.exists()) {
			generateStatusReportJson(json, "Folder does not exist");
			return;			
		}
		FeFile files[] = srcFile.listFiles();
		if (files == null || files.length == 0) {
			generateStatusReportJson(json, "success");
			JSONObject datajson = new JSONObject();
			datajson.put("path", file);	
			statusJson.put("data", datajson);
			return;
		}
		
		SortComparator.TypeComparator typeCompare = new SortComparator.TypeComparator(
				file);
		files = QuickSort.perform(files, 0, files.length - 1, typeCompare);
		
		JSONArray array = new JSONArray();
		for (FeFile entry : files) {
			long date = entry.lastModified();
			if (entry.isDirectory()) {
				if (showDir) {
					JSONObject obj = new JSONObject();
					obj.put("name", entry.getName());
					obj.put("type", "dir");
					obj.put("last", date);
					obj.put("empty", isDirEmpty(entry.getFile()));
					//obj.put("laststr", FeUtil.getLastModifiedString(date));
					array.put(obj);
				}
			} else {
				if (showFile) {
					JSONObject obj = new JSONObject();
					obj.put("name", entry.getName());
					obj.put("extendname", FileOperator.getExtendFileName(entry));
					obj.put("type", "file");
					obj.put("size", entry.length());
					obj.put("last", date);
					//obj.put("laststr", FeUtil.getLastModifiedString(date));
					array.put(obj);
				}
			}
		}
		generateStatusReportJson(json, "success");
		JSONObject datajson = new JSONObject();
		datajson.put("path", file);
		datajson.put("files", array);
		statusJson.put("data", datajson);
	}

	public void runList(String path) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", "file");
		json.put("type", "list");
		json.put("src", path);
		json.put("attr", "all");
		runList(json);
	}

	private void runDelete(JSONObject json) throws JSONException {
		JSONObject cmd = json.getJSONObject("data");
		String file = cmd.getString("src");
		if (!FileOperator.delete(new File(file))) {
			generateStatusReportJson(json, "Can not delete file");
		} else {
			generateStatusReportJson(json, "success");
		}
	}

	private void runCopy(JSONObject json) throws Exception {
		String src = json.getString("src");
		String dst = json.getString("dst");
		String mode = json.getString("attr");
		File srcFile = new File(getRoot(), src);
		File dstFile = new File(getRoot(), dst);
		boolean r;
		if (mode.equals("copy")) {
			r = FileOperator.copyTo(null, null, srcFile.getParent(), srcFile.getName(),
					dstFile.getAbsolutePath(), false, true, false);
		} else if (mode.equals("cut")) {
			r = FileOperator.copyTo(null, null, srcFile.getParent(), srcFile.getName(),
					dstFile.getAbsolutePath(), true, true, false);
		} else {
			throw new JSONException("No copy attr set");
		}
		if(r) {
			generateStatusReportJson(json, "success");
		} else {
			generateStatusReportJson(json, "copy failed");
		}
	}

	private void runCreate(JSONObject json) throws JSONException {
		JSONObject cmd = json.getJSONObject("data");
		String file = cmd.getString("src");
		FeFile dir = new FeFile(file);
		if (!dir.mkdir()) {
			generateStatusReportJson(json, "Can not create folder");
		} else {
			generateStatusReportJson(json, "success");
		}
	}

	private void runRename(JSONObject json) throws JSONException {
		JSONObject cmd = json.getJSONObject("data");
		String srcFile = cmd.getString("src");
		String dstFile = cmd.getString("dst");
		FeFile entry = new FeFile(srcFile);
		FeFile new_entry = new FeFile(dstFile);
		if (entry.renameTo(new_entry)) {
			generateStatusReportJson(json, "success");
		} else {
			generateStatusReportJson(json, "Can not rename");
		}
	}

	private void generateStatusReportJson(JSONObject json, String status)
			throws JSONException {
		statusJson = new JSONObject();
		statusJson.put("name", json.getString("name"));
		statusJson.put("type", json.getString("type"));
		statusJson.put("status", status);
	}
	
	private boolean isDirEmpty(File dir) {
		DirFilter filter = new DirFilter();
		File [] files = dir.listFiles(filter);
		if(files == null || files.length == 0) return true;
		return false;
	}
	
	private class DirFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if(pathname.isDirectory()) return true;
			return false;
		}
	}
}

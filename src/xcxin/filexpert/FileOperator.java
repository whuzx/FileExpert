package xcxin.filexpert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import xcxin.filexpert.Batch.FileCopyWorker;
import xcxin.filexpert.Batch.FileDeleteWorker;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FileOperator extends Object {

	public boolean m_copy_exit = false;
	public boolean m_overwrite = false;

	private static String[] video_extend_name = { "mp4", "rmvb", "rm", "avi",
			"3gp", "wmv", "mpeg" };
	private static String[] audio_extend_name = { "mp3", "wav" };
	private static String[] image_extend_name = { "jpg", "jpeg", "gif", "png" };

	public static String removeSmbPassword(String path) {
		// Update directory record
		if (path.indexOf("smb") == 0) {
			// We need to remove user name & password!!
			int start = path.indexOf('@') + 1;
			return "\\\\" + path.substring(start, path.length());
		} else {
			return path;
		}
	}

	public class DeleteHelper extends Thread implements Handler.Callback {

		private Context m_Context;
		private ProgressDialog m_pd = null;
		private Handler m_Handler = null;
		private FileOperator m_FileOper;
		private String m_path = null;
		private String m_name = null;
		public AtomicInteger m_atomic;
		private AtomicInteger ui_sync;

		public DeleteHelper(Context ctx, AtomicInteger sync) {
			super();
			m_Context = ctx;
			m_Handler = new Handler(this);
			m_FileOper = new FileOperator();
			m_atomic = new AtomicInteger(0);
			ui_sync = sync;
		}

		private void performDelete(Context ctx, ProgressDialog pd, String path,
				String name) {
			boolean r = false;
			m_atomic.set(0);
			if (path.indexOf("smb://") == 0) {
				FeFile file = new FeFile(path, name + "/");
				r = m_FileOper.delete(file, m_Handler, m_atomic, true);
				file = null;
			} else {
				FeFile file = new FeFile(path, name);
				r = m_FileOper.delete(file, m_Handler, m_atomic, true);
				file = null;
			}
			pd.dismiss();
			if (r == false) {
				Log.v("FE", "Could not delete files");
			}
			ui_sync.set(1);
			FeUtil.gc();
		}

		public void run() {
			performDelete(m_Context, m_pd, m_path, m_name);
		}

		public void setDelParams(String path, String name) {
			m_path = path;
			m_name = name;
		}

		@Override
		public void start() {
			m_pd = ProgressDialog.show(m_Context,
					m_Context.getString(R.string.deleting), m_path + "/"
							+ m_name);
			super.start();
		}

		@Override
		public boolean handleMessage(Message msg) {
			Bundle bdl = msg.getData();
			String dlg_msg = bdl.getString("del_msg");

			if (dlg_msg != null) {
				m_pd.setMessage(m_Context.getString(R.string.deal_with)
						+ dlg_msg);
				m_atomic.set(1);
			}

			dlg_msg = bdl.getString("update_view");
			if (dlg_msg != null) {
				((FileLister) m_Context).refresh();
			}

			dlg_msg = bdl.getString("del_error");
			if (dlg_msg != null) {
				((FileLister) m_Context).showInfo(
						((FileLister) m_Context).getString(R.string.del_error),
						((FileLister) m_Context).getString(R.string.error),
						false);
			}

			dlg_msg = bdl.getString("del_finish");
			if (dlg_msg != null) {
				m_pd.dismiss();
				m_atomic.set(1);
			}

			return true;
		}
	}

	public static boolean createFolder(String FolderName, String Path)
			throws OperatorFolderException {
		return createFolder(FolderName, Path, false);
	}

	public static boolean createFolder(String FolderName, String Path,
			boolean deleteIfExist) throws OperatorFolderException {
		FeFile operator;
		operator = new FeFile(Path, FolderName);
		if (operator.exists() && deleteIfExist) {
			operator.delete();
		} else if (operator.exists() && !deleteIfExist) {
			throw new OperatorFolderException("the folder [" + FolderName
					+ "] is already exsit!");
		}
		boolean r = operator.mkdir();
		operator = null;
		return r;
	}

	static class OperatorFolderException extends Exception {
		private static final long serialVersionUID = 1L;

		public OperatorFolderException(String message, Throwable cause) {
			super(message, cause);
		}

		public OperatorFolderException(String message) {
			super(message);
		}
	}

	public boolean delete(FeFile target, Handler handler, AtomicInteger ai,
			boolean topCall) {

		boolean result = false;

		if (target.isDirectory()) {
			FeFile[] files = target.listFiles();
			if (files == null)
				return true;
			for (int index = 0; index < files.length; index++) {
				result = delete(files[index], handler, ai, false);
				if (result == false)
					return false;
			}
		}

		if (target.isDirectory() == true) {
			Message msg = handler.obtainMessage();
			Bundle bdl = new Bundle();
			bdl.putString("del_msg", target.getName());
			msg.setData(bdl);
			handler.sendMessage(msg);

			// Wait for UI thread to update msg
			while (ai.get() == 0)
				;
			ai.set(0);
		}

		// Delete self
		try {
			result = target.delete();
		} catch (Exception e) {
			result = false;
		}
		if (result == false) {
			Message msg = handler.obtainMessage();
			Bundle bdl = new Bundle();
			bdl.putString("del_error", "error");
			msg.setData(bdl);
			handler.sendMessage(msg);
			return false;
		}

		if (topCall == true) {
			// Dismiss dlg
			ai.set(0);
			Message msg = handler.obtainMessage();
			Bundle bdl = new Bundle();
			bdl.putString("del_finish", "yes");
			msg.setData(bdl);
			handler.sendMessage(msg);
			while (ai.get() == 0)
				;
			ai.set(0);

			// Notify UI thread to update its view
			if (topCall == true) {
				notifyCallerUpdateView(msg, bdl, handler);
			}
		}

		return true;
	}

	public static boolean delete(File target) {

		boolean result = false;

		if (target.isDirectory()) {
			File[] files = target.listFiles();
			for (int index = 0; index < files.length; index++) {
				result = delete(files[index]);
				if (result == false)
					return false;
			}
		}

		// Delete self
		try {
			result = target.delete();
		} catch (Exception e) {
			result = false;
		}
		if (result == false) {
			return false;
		}

		return true;
	}

	public static boolean delete(FileDeleteWorker worker, String path,
			String name) {
		return delete(worker, new FeFile(path, name), false);
	}
	public static boolean delete(FileDeleteWorker worker, FeFile file,
			boolean hasup) {
		if (worker.isCancel()) {
			return false;
		}

		if (!hasup) {
			worker.updateProgressText(file.getName());
		}

		if (file.isDirectory()) {
			FeFile[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (int i = 0, length = files.length; i < length; i++) {
					FeFile ff = files[i];
					boolean b = delete(worker, ff, true);
					if (b) {
						worker.updateProgressValue();
					} else {
						return false;
					}
				}
			}
		}

		boolean b = file.delete();
		if (b) {
			worker.updateProgressValue();
		}
		return b;
	}
	public static boolean copyTo(FileCopyWorker worker, Context context, String src_path,
			String src_name, String dst_path, boolean del_after_copy,
			boolean topCall, boolean skipExisting) throws Exception {

		if (worker != null) {
			if (worker.isCancel()) {
				return false;
			}
		}

		FeFile src_file = new FeFile(src_path, src_name);
		FeFile dst_file = new FeFile(dst_path, src_name);
		if (src_file.exists() == false) {
			return false;
		}

		if (worker != null) {
			if (dst_file.isLocalFile()) {
				worker.updateProgressText(src_name + " -> " + dst_path);
			} else {
				worker.updateProgressText(src_name + " -> "
						+ removeSmbPassword(dst_path));
			}
		}

		if (dst_file.exists()) {
			if (worker != null) {
				worker.skip_existing = false;
				if (worker.auto_overwrite != true) {
					if (dst_file.isLocalFile() == true) {
						worker.alertUserAndWait(dst_path + "/" + src_name);
					} else {
						worker.alertUserAndWait(removeSmbPassword(dst_path)
								+ src_name);
					}
				}
				skipExisting = worker.skip_existing;
			}
		}

		// Does src is a dir?
		if (src_file.isDirectory() == true) {
			String src_full_path;
			if (src_path.compareTo("/") == 0) {
				src_full_path = src_path + src_name;
			} else {
				if (src_path.charAt(src_path.length() - 1) == '/') {
					src_full_path = src_path + src_name;
				} else {
					src_full_path = src_path + "/" + src_name;
				}
			}
			if (dst_path.length() >= src_full_path.length()) {
				String s = src_full_path + "/";
				String d = dst_path + "/";
				if (d.contains(s)) {
					if(worker != null && context != null) {
						worker.updateToastMessage(context.getString(R.string.dir_warning));
					}
					return false;
				}
			}

			if (del_after_copy == true) {
				if (src_file.isLocalFile() == true
						&& dst_file.isLocalFile() == true) {
					// Cut
					if (worker != null) {
						worker.updateProgressValueMax();
					}
					boolean r = src_file.renameTo(dst_file);
					if (r == false) {
						return false;
					}
					return true;
				}
			}

			// Yes, let's make dst dir and copy all items within src dir
			if (dst_file.exists() == false) {
				if (worker != null) {
					worker.updateProgressValue();
				}
				boolean r = createFolder(src_name, dst_path);
				if (r == false) {
					return false;
				}
			}

			String SrcDirContents[] = src_file.list();
			for (int index = 0; index < SrcDirContents.length; index++) {
				boolean r;
				if (dst_path.charAt(dst_path.length() - 1) != '/') {
					r = copyTo(worker, context, src_full_path, SrcDirContents[index],
							dst_path + "/" + src_name, del_after_copy, false,
							skipExisting);
				} else {
					r = copyTo(worker, context, src_full_path, SrcDirContents[index],
							dst_path + src_name, del_after_copy, false,
							skipExisting);
				}
				if (r == false) {
					SrcDirContents = null;
					FeUtil.gc();
					return false;
				}
			}

			FeUtil.gc();
			SrcDirContents = null;
			if (del_after_copy == true) {
				if (src_file.isLocalFile() == false
						|| dst_file.isLocalFile() == false) {
					if (worker != null) {
						worker.updateProgressValueMax();
					}
					boolean r = src_file.delete();
					if (r == false) {
						return false;
					}
				}
			}
			return true;
		} else if (src_file.isFile() == true) {
			//
			// Src if a file - Do normal file copy
			//
			if (worker != null) {
				worker.updateProgressValue();
			}
			if (dst_file.exists() == true && skipExisting == false) {
				// Overwrite, delete it first
				dst_file.delete();
			}
			if (del_after_copy == false) {
				if (!(skipExisting == true && dst_file.exists() == true)) {
					// Normal Copy
					if (src_file.isLocalFile() == true
							&& dst_file.isLocalFile() == true) {
						if (copyFileUseChannel(src_file, dst_file) == false) {
							return false;
						}
					} else {
						boolean r = copyFileUseStream(src_file, dst_file);
						if (r != true) {
							return false;
						}
					}
				}
			} else {
				if (!(skipExisting == true && dst_file.exists() == true)) {
					// Cut
					boolean r;
					if (src_file.isLocalFile() == true
							&& dst_file.isLocalFile() == true) {
						r = src_file.renameTo(dst_file);
					} else {
						r = copyFileUseStream(src_file, dst_file);
						if (r == false) {
							return false;
						}
						r = src_file.delete();
					}
					if (r == false) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void notifyCallerUpdateView(Message msg, Bundle bdl, Handler handler) {
		// Notify UI thread to update its view
		msg = handler.obtainMessage();
		bdl.clear();
		bdl.putString("update_view", "yes");
		msg.setData(bdl);
		handler.sendMessage(msg);
	}

	public static boolean rename(String orig_name, String new_name, String path) {
		FeFile Operator1 = new FeFile(path, orig_name);
		FeFile Operator2 = new FeFile(path, new_name);
		if (Operator1.exists() == false) {
			Operator1 = null;
			Operator2 = null;
			return false;
		}
		if (Operator2.exists() == true) {
			Operator1 = null;
			Operator2 = null;
			return false;
		}
		try {
			boolean r = Operator1.renameTo(Operator2);
			Operator1 = null;
			Operator2 = null;
			return r;
		} catch (Exception e) {
			Operator1 = null;
			Operator2 = null;
			return false;
		}
	}

	public static long getSize(String path, String name) {
		FeFile Operator = new FeFile(path, name);
		if (Operator.exists() == false) {
			Operator = null;
			return -1;
		}
		if (Operator.isDirectory() == true) {
			Operator = null;
			return -1; // Currently we cannot display size of a directory
		}
		long size = Operator.length();
		Operator = null;
		return size;
	}

	public static boolean install_apk(FeFile apk, Activity parent) {
		return open_file_with_type(apk,
				"application/vnd.android.package-archive", parent);
	}

	private static boolean open_video(FeFile video_file, Activity parent) {
		return open_file_with_type(video_file, "video/*", parent);
	}

	private static boolean open_audio(FeFile audio_file, Activity parent) {
		return open_file_with_type(audio_file, "audio/*", parent);
	}

	private static boolean open_image_jpeg_jpg(FeFile img_file, Activity parent) {
		if (img_file.isLocalFile() == false) {
			Intent i = new Intent(parent, ImageViewer.class);
			i.putExtra(ImageViewer.FILE_NAME, img_file.getPath());
			parent.startActivity(i);
			return true;
		}
		return open_file_with_type(img_file, "image/jpeg", parent);
	}

	private static boolean open_image_png(FeFile img_file, Activity parent) {
		if (img_file.isLocalFile() == false) {
			Intent i = new Intent(parent, ImageViewer.class);
			i.putExtra(ImageViewer.FILE_NAME, img_file.getPath());
			parent.startActivity(i);
			return true;
		}
		return open_file_with_type(img_file, "image/png", parent);
	}

	private static boolean open_image_gif(FeFile img_file, Activity parent) {
		return open_file_with_type(img_file, "image/gif", parent);
	}

	private static boolean open_odt_file(FeFile odt_file, Activity parent) {
		return open_file_with_type(odt_file,
				"application/vnd.oasis.opendocument.text", parent);
	}

	private static boolean open_msword(FeFile word_file, Activity parent) {
		return open_file_with_type(word_file, "application/msword", parent);
	}

	private static boolean open_msExcel(FeFile word_file, Activity parent) {
		return open_file_with_type(word_file, "application/vnd.ms-excel",
				parent);
	}

	private static boolean open_msppt(FeFile word_file, Activity parent) {
		return open_file_with_type(word_file, "application/vnd.ms-powerpoint",
				parent);
	}

	private static boolean open_pdf(FeFile pdf, Activity parent) {
		return open_file_with_type(pdf, "application/pdf", parent);
	}

	private static boolean open_unknown_file(FeFile file, Activity parent) {
		return open_file_with_type(file, "*/*", parent);
	}

	private static boolean open_txt(FeFile txt_file, Activity parent) {
		Intent i = new Intent(parent, TxtViewer.class);
		i.putExtra(TxtViewer.FILE_NAME, txt_file.getPath());
		parent.startActivity(i);
		return true;
	}

	private static boolean open_html(FeFile html_file, Activity parent) {
		return open_file_with_type(html_file, "text/html", parent);
	}

	private static boolean open_xml(FeFile xml_file, Activity parent) {
		return open_file_with_type(xml_file, "text/xml", parent);
	}

	public static boolean open_file_with_type(FeFile target, String type,
			Activity parent) {
		if (parent == null || target == null)
			return false;
		if (target.isLocalFile() == true) {
			// Local open
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setDataAndType(Uri.fromFile(target.getFile()), type);
			parent.startActivity(i);
			return true;
		} else {
			// Network open
			return ((FileLister) parent).startSmbStreamService(target, type);
		}
	}

	protected static boolean copyFileUseChannel(FeFile src, FeFile dst)
			throws Exception {

		if (src.isLocalFile() != true || dst.isLocalFile() != true)
			return false;
		
		if (!src.getFile().canRead()
				|| !dst.getFile().getParentFile().canWrite()) {
			return RootShell.copyFile(src.getFile(), dst.getFile());
		}

		long length = 32 * 1024; // 4096 bytes per one operation;
		FileInputStream in = new FileInputStream(src.getFile());
		FileOutputStream out = new FileOutputStream(dst.getFile());
		FileChannel inC = in.getChannel();
		FileChannel outC = out.getChannel();
		ByteBuffer b = null;

		while (true) {
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
				return true;
			}
			if ((inC.size() - inC.position()) < length) {
				length = inC.size() - inC.position();
			} else {
				length = 512 * 1024;
			}
			b = ByteBuffer.allocateDirect((int) length);
			inC.read(b);
			b.flip();
			outC.write(b);
			outC.force(false);
			b = null;
			FeUtil.gc();
		}
	}

	public static boolean perform_file_operation(FeFile target, Activity parent) {
		try {
			if (target.isFile()) {
				String name = target.getName().toLowerCase();
				if (target.isLocalFile() == true) {
					name = name.substring(name.lastIndexOf('.') + 1,
							name.length());
				} else {
					name = name.substring(name.lastIndexOf('.') + 1,
							name.length() - 1);
				}
				if (name.toLowerCase().compareTo("apk") == 0) {
					return install_apk(target, parent);
				} else if (name.toLowerCase().compareTo("mp4") == 0) {
					return open_video(target, parent);
				} else if (name.toLowerCase().compareTo("3gp") == 0) {
					return open_video(target, parent);
				} else if (name.toLowerCase().compareTo("wmv") == 0) {
					return open_video(target, parent);
				} else if (name.toLowerCase().compareTo("avi") == 0) {
					return open_video(target, parent);
				} else if (name.toLowerCase().compareTo("rm") == 0) {
					return open_video(target, parent);
				} else if (name.toLowerCase().compareTo("rmvb") == 0) {
					return open_video(target, parent);
				} else if (name.toLowerCase().compareTo("wav") == 0) {
					return open_audio(target, parent);
				} else if (name.toLowerCase().compareTo("mp3") == 0) {
					return open_audio(target, parent);
				} else if (name.toLowerCase().compareTo("pdf") == 0) {
					return open_pdf(target, parent);
				} else if (name.toLowerCase().compareTo("doc") == 0) {
					return open_msword(target, parent);
				} else if (name.toLowerCase().compareTo("xls") == 0) {
					return open_msExcel(target, parent);
				} else if (name.toLowerCase().compareTo("ppt") == 0) {
					return open_msppt(target, parent);
				} else if (name.toLowerCase().compareTo("docx") == 0) {
					return open_msword(target, parent);
				} else if (name.toLowerCase().compareTo("txt") == 0) {
					return open_txt(target, parent);
				} else if (name.toLowerCase().compareTo("ini") == 0) {
					return open_txt(target, parent);
				} else if (name.toLowerCase().compareTo("jpg") == 0) {
					return open_image_jpeg_jpg(target, parent);
				} else if (name.toLowerCase().compareTo("jpeg") == 0) {
					return open_image_jpeg_jpg(target, parent);
				} else if (name.toLowerCase().compareTo("png") == 0) {
					return open_image_png(target, parent);
				} else if (name.toLowerCase().compareTo("gif") == 0) {
					return open_image_gif(target, parent);
				} else if (name.toLowerCase().compareTo("html") == 0) {
					return open_html(target, parent);
				} else if (name.toLowerCase().compareTo("htm") == 0) {
					return open_html(target, parent);
				} else if (name.toLowerCase().compareTo("xml") == 0) {
					return open_xml(target, parent);
				} else if (name.toLowerCase().compareTo("odt") == 0) {
					return open_odt_file(target, parent);
				} else {
					return open_unknown_file(target, parent);
				}
			} else {
				// it's directory
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean extendFileNameCompare(FeFile target, String ExtendName) {
		String name = target.getName().toLowerCase();
		name = name.substring(name.lastIndexOf('.') + 1, name.length());
		if (name.compareTo(ExtendName) == 0) {
			return true;
		}

		return false;
	}

	public static boolean extendFileNameCompare(String name, String ExtendName) {
		String str = name.toLowerCase();
		str = str.substring(name.lastIndexOf('.') + 1, str.length());
		if (str.compareTo(ExtendName) == 0) {
			return true;
		}
		return false;
	}

	public static String getExtendFileName(File target) {
		String name = target.getName().toLowerCase();
		name = name.substring(name.lastIndexOf('.') + 1, name.length());
		return name;
	}

	public static String getExtendFileName(FeFile target) {
		String name = target.getName().toLowerCase();
		if (name.toCharArray()[name.length() - 1] != '/') {
			name = name.substring(name.lastIndexOf('.') + 1, name.length());
		} else {
			name = name.substring(name.lastIndexOf('.') + 1, name.length() - 1);
		}
		return name;
	}

	public boolean isFileType(FeFile target, String[] type_set) {
		for (int index = 0; index < type_set.length; index++) {
			if (extendFileNameCompare(target, type_set[index]) == true) {
				return true;
			}
		}
		return false;
	}

	public static boolean isFileType(String name, String[] type_set) {
		for (int index = 0; index < type_set.length; index++) {
			if (extendFileNameCompare(name, type_set[index]) == true) {
				return true;
			}
		}
		return false;
	}

	public boolean isAudioFile(FeFile target) {
		return isFileType(target, audio_extend_name);
	}

	public static boolean isAudioFile(String name) {
		return isFileType(name, audio_extend_name);
	}

	public boolean isVideoFile(FeFile target) {
		return isFileType(target, video_extend_name);
	}

	public static boolean isVideoFile(String name) {
		return isFileType(name, video_extend_name);
	}

	public boolean isApkPackage(FeFile target) {
		return extendFileNameCompare(target, "apk");
	}

	public static boolean isApkPackage(String name) {
		return extendFileNameCompare(name, "apk");
	}

	public boolean isZipFile(FeFile target) {
		return extendFileNameCompare(target, "zip");
	}

	public static boolean isZipFile(String name) {
		return extendFileNameCompare(name, "zip");
	}

	public boolean isRarFile(FeFile target) {
		return extendFileNameCompare(target, "rar");
	}

	public static boolean isRarFile(String name) {
		return extendFileNameCompare(name, "rar");
	}

	public boolean isPdfFile(FeFile target) {
		return extendFileNameCompare(target, "pdf");
	}

	public static boolean isPdfFile(String name) {
		return extendFileNameCompare(name, "pdf");
	}

	public boolean isWordFile(FeFile target) {
		boolean r = extendFileNameCompare(target, "doc");
		if (r == false) {
			return extendFileNameCompare(target, "docx");
		}
		return true;
	}

	public static boolean isWordFile(String name) {
		boolean r = extendFileNameCompare(name, "doc");
		if (r == false) {
			return extendFileNameCompare(name, "docx");
		}
		return true;
	}

	public boolean isPowerPointFile(FeFile target) {
		return extendFileNameCompare(target, "ppt");
	}

	public static boolean isPowerPointFile(String name) {
		return extendFileNameCompare(name, "ppt");
	}

	public boolean isExcelFile(FeFile target) {
		return extendFileNameCompare(target, "xls");
	}

	public static boolean isExcelFile(String name) {
		return extendFileNameCompare(name, "xls");
	}

	public boolean isImageFile(FeFile target) {
		return isFileType(target, image_extend_name);
	}

	public static boolean isImageFile(String name) {
		return isFileType(name, image_extend_name);
	}

	public static boolean copyFileUseStream(FeFile src, FeFile dst) {
		// int length = 2 * 1024 * 1024;
		long length = 32 * 1024;
		FeFileInputStream in = new FeFileInputStream(src);
		FeFileOutputStream out = new FeFileOutputStream(dst);
		BufferedInputStream bis = new BufferedInputStream(in.getInputStream());
		BufferedOutputStream bos = new BufferedOutputStream(
				out.getOutputStream());

		if (length > src.length() && src.length() > 0) {
			length = src.length();
		}
		byte[] buffer = new byte[(int) length];
		int bytes_read;
		try {
			while (true) {
				bytes_read = bis.read(buffer);
				if (bytes_read == -1) {
					bis.close();
					bos.flush();
					bos.close();
					bis = null;
					bos = null;
					in = null;
					out = null;
					buffer = null;
					FeUtil.gc();
					return true;
				} else {
					bos.write(buffer, 0, bytes_read);
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static String getContentType(String extendName) {
		if (extendName.toLowerCase().compareTo("apk") == 0) {
			return "application/vnd.android.package-archive";
		} else if (extendName.toLowerCase().compareTo("mp4") == 0) {
			return "video/mp4";
		} else if (extendName.toLowerCase().compareTo("3gp") == 0) {
			return "video/3gpp";
		} else if (extendName.toLowerCase().compareTo("avi") == 0) {
			return "video/x-msvideo";
		} else if (extendName.toLowerCase().compareTo("rm") == 0) {
			return "video/rm";
		} else if (extendName.toLowerCase().compareTo("rmvb") == 0) {
			return "application/vnd.rn-realmedia-vbr";
		} else if (extendName.toLowerCase().compareTo("wav") == 0) {
			return "audeo/wav";
		} else if (extendName.toLowerCase().compareTo("wmv") == 0) {
			return "video/x-ms-wmv";
		} else if (extendName.toLowerCase().compareTo("mp3") == 0) {
			return "audio/mp3";
		} else if (extendName.toLowerCase().compareTo("doc") == 0) {
			return "application/msword";
		} else if (extendName.toLowerCase().compareTo("docx") == 0) {
			return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		} else if (extendName.toLowerCase().compareTo("ppt") == 0) {
			return "application/vnd.ms-powerpoint";
		} else if (extendName.toLowerCase().compareTo("xls") == 0) {
			return "application/vnd.ms-excel";
		} else if (extendName.toLowerCase().compareTo("txt") == 0) {
			return "text/plain";
		} else if (extendName.toLowerCase().compareTo("ini") == 0) {
			return "text/plain";
		} else if (extendName.toLowerCase().compareTo("jpg") == 0) {
			return "image/jpeg";
		} else if (extendName.toLowerCase().compareTo("jpeg") == 0) {
			return "image/jpeg";
		} else if (extendName.toLowerCase().compareTo("png") == 0) {
			return "image/png";
		} else if (extendName.toLowerCase().compareTo("gif") == 0) {
			return "image/gif";
		} else if (extendName.toLowerCase().compareTo("html") == 0) {
			return "text/html";
		} else if (extendName.toLowerCase().compareTo("htm") == 0) {
			return "text/html";
		} else if (extendName.toLowerCase().compareTo("xml") == 0) {
			return "text/xml";
		} else {
			return "application/octet-stream";
		}
	}
}

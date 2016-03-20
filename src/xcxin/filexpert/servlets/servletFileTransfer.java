package xcxin.filexpert.servlets;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.FileEntity;

import xcxin.filexpert.FileOperator;
import xcxin.filexpert.WebServer.FeServletBase;

public class servletFileTransfer extends FeServletBase {

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		send404IfNeeded(outstream);
	}
	
	@Override
	public AbstractHttpEntity getEntity() {
		if (getFile().exists()) {
			String contentType = FileOperator.getContentType(FileOperator
					.getExtendFileName(getFile()));
			FileEntity entity = new FileEntity(getFile(), contentType);
			return entity;
		}
		return super.getEntity();
	}
	
	@Override
	public void execute() {
		if (getFile().exists()) {
			set404(true);
		} else {
			set404(false);
		}
	}

	@Override
	public boolean isContentTypeSet() {
		if (getFile().exists()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getContentType() {
		if (getFile().exists()) {
			return FileOperator.getContentType(FileOperator
					.getExtendFileName(getFile()));
		}
		return super.getContentType();
	}
}

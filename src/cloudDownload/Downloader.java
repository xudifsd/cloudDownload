package cloudDownload;

import java.io.File;
import java.net.URI;
import java.sql.SQLException;

import static cloudDownload.Db.startDownload;
import static cloudDownload.Db.changeState;
import static cloudDownload.Db.finishDownload;
import cloudDownload.CloudCache.CopyInfo;
import cloudDownload.Db.State;

public abstract class Downloader implements Runnable {
	protected int id;
	protected URI uri;
	protected CloudCache cc;

	static class DownloadInfo {
		public File file;
		public String ext;
		public DownloadInfo(File f, String e) {
			file = f;
			ext = e;
		}
	}

	public Downloader(int id, URI uri, CloudCache cc) {
		this.id = id;
		this.uri = uri;
		this.cc = cc;
	}

	public abstract DownloadInfo doDownload() throws Exception ;// method should update progress periodically

	public void run() {
		try {
			startDownload(id);
			DownloadInfo info = doDownload();
			CopyInfo cinfo = cc.copyToCC(info);
			finishDownload(id, cinfo.retrieveUrl, cinfo.size);
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				changeState(id, State.failed, ex.getMessage());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
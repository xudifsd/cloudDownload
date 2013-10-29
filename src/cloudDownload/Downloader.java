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

	public Downloader(int id, URI uri, CloudCache cc) {
		this.id = id;
		this.uri = uri;
		this.cc = cc;
	}

	public abstract File doDownload() throws Exception ;// method should update progress periodically

	public void run() {
		try {
			startDownload(id);
			File file = doDownload();
			CopyInfo info = cc.copyToCC(file);
			finishDownload(id, info.retrieveUrl, info.size);
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
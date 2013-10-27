package cloudDownload;

import java.io.File;
import java.net.URI;
import java.sql.SQLException;

import static cloudDownload.Db.startDownload;
import static cloudDownload.Db.changeState;
import static cloudDownload.Db.finishDownload;
import cloudDownload.Db.State;

public abstract class Downloader implements Runnable {
	private int id;
	private URI uri;
	private CloudCache cc;

	public Downloader(int id, URI uri) {
		this.id = id;
		this.uri = uri;
	}

	public abstract File doDownload();// method should update progress periodically

	public void run() {
		try {
			startDownload(id);
			File file = doDownload();
			String retrieveUrl = cc.copyToCC(file);
			finishDownload(id, retrieveUrl);
		} catch (Exception ex) {
			try {
				changeState(id, State.failed);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
package cloudDownload;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import static cloudDownload.Db.sumSize;
import static cloudDownload.Utils.getMD5Checksum;
import static cloudDownload.Utils.zipIt;
import cloudDownload.Db;
import cloudDownload.Downloader.DownloadInfo;

public class CloudCache {
	public long sizeThreshold;
	public long actualSize;
	private final static String uniqName = "this_must_be_uniq";

	public static class CopyInfo {
		String retrieveUrl;
		long size;

		public CopyInfo(String retrieveUrl, long size) {
			this.retrieveUrl = retrieveUrl;
			this.size = size;
		}
	}

	public CloudCache(long sizeThreshold) throws SQLException {
		this.sizeThreshold = sizeThreshold;
		actualSize = sumSize();
	}

	private void freeDisk(long sizeToFree) throws Exception {
		// because freeDisk is only called from copyToCC, and it's synchronized
		// so freeDisk is synchronized inherent
		List<Db.RemovalInfo> listToRemove = Db.gatherRemovalInfo(sizeToFree);
		Iterator<Db.RemovalInfo> it = listToRemove.iterator();

		while (it.hasNext()) {
			Db.RemovalInfo info = it.next();
			Db.emptyTask(info.id);
			File file = new File(Config.fileContainer + info.retrieveURL);
			if (!file.delete())
				throw new Exception("error when deleting " + Config.fileContainer + info.retrieveURL);
		}
	}

	public synchronized CopyInfo copyToCC(DownloadInfo info) throws Exception {
		// we don't update retrieve URL in db, it's the job of Downloader
		if (info.file.isDirectory()) {
			zipIt(uniqName, info.file.getAbsolutePath());
			info.file = new File(uniqName);
			info.ext = ".zip";
		}

		long size = info.file.length();
		if (size + actualSize > sizeThreshold) {
			System.out.format("size = %d, actualSize = %d, sizeThreshold = %d\n", size, actualSize, sizeThreshold);
			freeDisk(size - (sizeThreshold - actualSize));//only free necessary disk
		}

		String md5 = getMD5Checksum(info.file.getAbsolutePath());

		Utils.moveFile(info.file.getAbsolutePath(), Config.fileContainer + md5 + info.ext);
		return new CopyInfo(md5 + info.ext, size);
	}
}
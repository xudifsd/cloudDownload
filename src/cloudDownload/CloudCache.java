package cloudDownload;

import java.io.File;

import java.sql.SQLException;

import static cloudDownload.Db.sumSize;
import static cloudDownload.Utils.getMD5Checksum;
import static cloudDownload.Utils.zipIt;

public class CloudCache {
	private long sizeThreshold;
	private long actualSize;
	private final static String uniqName = "this_must_be_uniq";

	public CloudCache(long sizeThreshold) throws SQLException {
		this.sizeThreshold = sizeThreshold;
		actualSize = sumSize();
	}

	private void freeDisk(long sizeToFree) {
		//TODO use LFU algorithm to delete file from disk and empty size in Db
	}

	public synchronized String copyToCC(File file) throws Exception {
		// we don't update retrieve URL in db, it's the job of Downloader
		if (file.isDirectory()) {
			zipIt(uniqName, file.getAbsolutePath());
			file = new File(uniqName);
		}

		long size = file.length();
		if (size + actualSize > sizeThreshold)
			freeDisk(size - (sizeThreshold - actualSize));//only free necessary disk

		String md5 = getMD5Checksum(file.getAbsolutePath());
		File destFile = new File(Config.fileContainer + md5);
		if (!file.renameTo(destFile))
			throw new Exception("Something wrong when renaming");
		return destFile.getName();
	}
}
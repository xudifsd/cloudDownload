package cloudDownload;

import java.io.File;
import java.sql.SQLException;

import static cloudDownload.Db.sumSize;
import static cloudDownload.Utils.getMD5Checksum;

public class CloudCache {
	private long sizeThreshold;
	private long actualSize;

	public CloudCache(long sizeThreshold) throws SQLException {
		this.sizeThreshold = sizeThreshold;
		actualSize = sumSize();
	}

	private File tarIt(File input) {
		//TODO make a tar file from original one
		return new File("/etc/passwd");//dummy
	}

	private void freeDisk(long sizeToFree) {
		//TODO use LFU algorithm to delete file from disk and empty size in Db
	}

	public synchronized String copyToCC(File file) throws Exception {
		// we don't update retrieve URL in db
		if (file.isDirectory())
			file = tarIt(file);

		long size = file.length();
		if (size + actualSize > sizeThreshold)
			freeDisk(size - (sizeThreshold - actualSize));//only free necessary disk

		String md5 = getMD5Checksum(file.getAbsolutePath());
		File destFile = new File(md5);// TODO make sure move it to dest dir
		file.renameTo(destFile);
		return destFile.getName();
	}
}
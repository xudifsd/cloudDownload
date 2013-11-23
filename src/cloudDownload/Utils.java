package cloudDownload;

import java.io.*;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Utils {
	private static byte[] createChecksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0)
				complete.update(buffer, 0, numRead);
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}

	public static String getMD5Checksum(String filename) throws Exception {
		byte[] b = createChecksum(filename);
		StringBuilder result = new StringBuilder(b.length);

		for (int i = 0; i < b.length; i++)
			result.append(Integer.toString((b[i] & 0xff) + 0x100, 16)
					.substring(1));
		return result.toString();
	}

	public static void zipIt(String dest, String source) throws IOException,
			InterruptedException {
		Process p = Runtime.getRuntime().exec("zip -r " + dest + " " + source);
		p.waitFor();
	}

	public static void initSystem() throws Exception {
		Db.initDb();

		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
		stpe.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					Db.penalizeHit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}, 60 * 60, 60 * 60, TimeUnit.SECONDS);

		File theDir = new File(Config.fileContainer);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			if (!theDir.mkdir())
				throw new Exception("failed to create " + Config.fileContainer);
		}

		// make sure system have zip and wget command installed
		Runtime.getRuntime().exec("zip");
		Runtime.getRuntime().exec("axel");
	}

	public static void moveFile(String inPath, String outPath)
			throws IOException {

		InputStream in = null;
		OutputStream out = null;

		File oldFile = new File(inPath);
		File newFile = new File(outPath);

		in = new FileInputStream(oldFile);
		out = new FileOutputStream(newFile);

		byte[] moveBuff = new byte[1024];
		int butesRead;

		while ((butesRead = in.read(moveBuff)) > 0)
			out.write(moveBuff, 0, butesRead);

		in.close();
		out.close();

		oldFile.delete();
	}

	public static String retrieveExt(String url) throws Exception {
		int index = url.lastIndexOf(".");
		if (index == -1)
			return "";
		else
			return url.substring(index);
	}
}

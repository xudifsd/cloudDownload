package cloudDownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPDownloader extends Downloader {
	public HTTPDownloader(int id, URI uri, CloudCache cc) {
		super(id, uri, cc);
	}

	@Override
	public DownloadInfo doDownload() throws Exception {
		File tmpFile = File.createTempFile("cloud", ".tmp", new File("."));
		tmpFile.delete();
		Process p = Runtime.getRuntime().exec(
				"axel " + uri + " -o " + tmpFile.getAbsolutePath());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		int progress = 0;
		String line = null;
		while ((line = reader.readLine()) != null) {
			Pattern re = Pattern.compile("^\\[ *([0-9]+)%\\].*$");
			Matcher m = re.matcher(line);

			if (m.matches()) {
				int cur = Integer.valueOf(m.group(1));
				if (cur > progress + 2) {
					try {
						Db.updateProgress(id, cur);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					progress = cur;
				}
			}
		}
		p.waitFor();
		if (p.exitValue() != 0)
			throw new Exception("HTTPDownload failed, URL is " + uri.toString());
		return new DownloadInfo(tmpFile, Utils.retrieveExt(uri.toString()));
	}
}
package cloudDownload;

import java.net.URI;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/* *
 * This class is the entry point to the base from webAPI server
 * */
public class TaskDispatcher {
	private ScheduledThreadPoolExecutor pool;
	private CloudCache cc;

	public TaskDispatcher(int numWorker) throws Exception {
		this.pool = new ScheduledThreadPoolExecutor(numWorker);
		Utils.initSystem();
		this.cc = new CloudCache(2 * 1024 * 1024);
	}

	public int newDownload(String URL) throws Exception {
		Db.TaskInfo info = Db.newTask(URL);

		if (info.isNew) {
			URI uri = new URI(URL);

			if (uri.getScheme().toLowerCase().equals("http")) {
				this.pool.submit(new HTTPDownloader(info.id, uri, this.cc));
				return info.id;
			} else
				throw new Exception("Unspported scheme");
		} else
			return info.id;
	}
}
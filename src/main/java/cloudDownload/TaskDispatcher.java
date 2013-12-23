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
		this.cc = new CloudCache(2l * 1024 * 1024 * 1024); //2G
	}

	public int newDownload(String URL) throws Exception {
		Db.TaskInfo info = Db.newTask(URL);

		if (info.isNew) {
			URI uri = new URI(URL);

			if (uri.getScheme().toLowerCase().equals("http")) {
				this.pool.submit(new HTTPDownloader(info.id, uri, this.cc));
				return info.id;
			} else {
				Db.changeState(info.id, Db.State.failed, "Unspported scheme");
				throw new Exception("Unspported scheme");
			}
		} else
			return info.id;
	}
}
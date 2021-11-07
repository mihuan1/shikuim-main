package cn.xyz.mianshi.scheduleds;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

/**
 * @author lidaye
 *
 */
public abstract class TimerTask implements Runnable{
	private ScheduledFuture<?> future = null;

	//private  Logger logger = LoggerFactory.getLogger(TimerTask.class.getName());


	public void setScheduledFuture(ScheduledFuture<?> future) {
		this.future = future;
	}

	public boolean isScheduled() {
		return future != null && !future.isCancelled() && !future.isDone();
	}
	
	public void cancel() {
		cancel(false);
	}

	public void cancel(boolean mayInterruptIfRunning) {
		if (future != null && !future.isDone()) {
			future.cancel(mayInterruptIfRunning);
		}
	}
}

package nz.co.trineo.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.dropwizard.lifecycle.Managed;

public class JobExecutionService implements Managed {

	private final ExecutorService service = Executors.newSingleThreadExecutor();

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
		service.shutdown();
	}

	public void scheduleJob(Callable<Void> task) {
		service.submit(task);
	}
}

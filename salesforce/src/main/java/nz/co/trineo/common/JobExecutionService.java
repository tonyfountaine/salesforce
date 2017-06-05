package nz.co.trineo.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jvnet.hk2.annotations.Service;

import io.dropwizard.lifecycle.Managed;

@Service
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

package nz.co.trineo;

import static nz.co.trineo.common.ServiceRegistry.registerService;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import io.dropwizard.setup.Environment;
import nz.co.trineo.common.JobExecutionService;
import nz.co.trineo.git.GitService;
import nz.co.trineo.github.GitHubService;
import nz.co.trineo.salesforce.SalesforceScheduleManager;
import nz.co.trineo.salesforce.SalesforceService;
import nz.co.trineo.salesforce.jobs.RefreshBackupsTask;
import nz.co.trineo.trello.TrelloService;

public final class RegisterDropwizardServices implements ContainerLifecycleListener {
	@Override
	public void onStartup(final Container container) {
		final ServiceLocator serviceLocator = container.getApplicationHandler().getServiceLocator();

		final Environment environment = serviceLocator.getService(Environment.class);

		final SalesforceService salesforceService = serviceLocator.getService(SalesforceService.class);
		final GitService gitService = serviceLocator.getService(GitService.class);
		final JobExecutionService jobExecutionService = serviceLocator.getService(JobExecutionService.class);
		final TrelloService trelloService = serviceLocator.getService(TrelloService.class);
		final GitHubService gitHubService = serviceLocator.getService(GitHubService.class);
		final RefreshBackupsTask refreshBackupsTask = serviceLocator.getService(RefreshBackupsTask.class);
		final SalesforceScheduleManager salesforceScheduleManager = serviceLocator
				.getService(SalesforceScheduleManager.class);

		registerService(trelloService);
		registerService(salesforceService);
		registerService(gitHubService);
		registerService(gitService);

		environment.admin().addTask(refreshBackupsTask);
		environment.lifecycle().manage(salesforceScheduleManager);
		environment.lifecycle().manage(jobExecutionService);
	}

	@Override
	public void onReload(final Container container) {
	}

	@Override
	public void onShutdown(final Container container) {
	}
}
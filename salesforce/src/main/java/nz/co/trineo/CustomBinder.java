package nz.co.trineo;

import javax.validation.Validator;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.hibernate.SessionFactory;

import com.codahale.metrics.MetricRegistry;

import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.AccountService;
import nz.co.trineo.common.ClientDAO;
import nz.co.trineo.common.ClientService;
import nz.co.trineo.common.JobExecutionService;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitProcessDAO;
import nz.co.trineo.git.GitService;
import nz.co.trineo.github.GitHubService;
import nz.co.trineo.repo.RepoDAO;
import nz.co.trineo.salesforce.BackupDAO;
import nz.co.trineo.salesforce.OrganizationDAO;
import nz.co.trineo.salesforce.SalesforceScheduleManager;
import nz.co.trineo.salesforce.SalesforceService;
import nz.co.trineo.salesforce.TestRunDAO;
import nz.co.trineo.salesforce.jobs.RefreshBackupsTask;
import nz.co.trineo.trello.BoardDAO;
import nz.co.trineo.trello.TrelloService;

public final class CustomBinder extends AbstractBinder {
	private final SessionFactory sessionFactory;
	private final Environment environment;
	private final AppConfiguration configuration;

	public CustomBinder(final SessionFactory sessionFactory, final Environment environment,
			final AppConfiguration configuration) {
		this.sessionFactory = sessionFactory;
		this.environment = environment;
		this.configuration = configuration;
	}

	@Override
	protected void configure() {
		bind(configuration).to(AppConfiguration.class);
		bind(environment).to(Environment.class);
		bind(environment.lifecycle()).to(LifecycleEnvironment.class);
		bind(environment.metrics()).to(MetricRegistry.class);
		bind(environment.getValidator()).to(Validator.class);

		bind(sessionFactory).to(SessionFactory.class);

		bind(GitProcessDAO.class).to(GitProcessDAO.class);
		bind(OrganizationDAO.class).to(OrganizationDAO.class);
		bind(AccountDAO.class).to(AccountDAO.class);
		bind(TestRunDAO.class).to(TestRunDAO.class);
		bind(BackupDAO.class).to(BackupDAO.class);
		bind(RepoDAO.class).to(RepoDAO.class);
		bind(ClientDAO.class).to(ClientDAO.class);
		bind(BoardDAO.class).to(BoardDAO.class);

		bind(GitService.class).to(GitService.class);
		bind(ClientService.class).to(ClientService.class);
		bind(GitHubService.class).to(GitHubService.class);
		bind(SalesforceService.class).to(SalesforceService.class);
		bind(AccountService.class).to(AccountService.class);
		bind(TrelloService.class).to(TrelloService.class);

		bind(JobExecutionService.class).to(JobExecutionService.class);
		bind(RefreshBackupsTask.class).to(RefreshBackupsTask.class);
		bind(SalesforceScheduleManager.class).to(SalesforceScheduleManager.class);
	}
}
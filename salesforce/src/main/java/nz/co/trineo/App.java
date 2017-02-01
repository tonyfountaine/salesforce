package nz.co.trineo;

import org.hibernate.SessionFactory;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import nz.co.anzac.dropwizard.quartz.QuartzBundle;
import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.AccountResource;
import nz.co.trineo.common.AccountService;
import nz.co.trineo.common.ClientDAO;
import nz.co.trineo.common.ClientResource;
import nz.co.trineo.common.ClientService;
import nz.co.trineo.common.ServiceRegistry;
import nz.co.trineo.common.ServiceResource;
import nz.co.trineo.common.StaticResource;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitProcessDAO;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.git.model.GitTask;
import nz.co.trineo.github.GitHubService;
import nz.co.trineo.repo.RepoDAO;
import nz.co.trineo.repo.RepoResource;
import nz.co.trineo.repo.model.Branch;
import nz.co.trineo.repo.model.Repository;
import nz.co.trineo.repo.model.Tag;
import nz.co.trineo.salesforce.BackupDAO;
import nz.co.trineo.salesforce.OrganizationDAO;
import nz.co.trineo.salesforce.RefreshBackupsTask;
import nz.co.trineo.salesforce.SalesforceResource;
import nz.co.trineo.salesforce.SalesforceScheduleManager;
import nz.co.trineo.salesforce.SalesforceService;
import nz.co.trineo.salesforce.TestRunDAO;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.CodeCoverageResult;
import nz.co.trineo.salesforce.model.CodeCoverageWarning;
import nz.co.trineo.salesforce.model.CodeLocation;
import nz.co.trineo.salesforce.model.Organization;
import nz.co.trineo.salesforce.model.RunTestFailure;
import nz.co.trineo.salesforce.model.RunTestSuccess;
import nz.co.trineo.salesforce.model.RunTestsResult;
import nz.co.trineo.trello.BoardDAO;
import nz.co.trineo.trello.TrelloResource;
import nz.co.trineo.trello.TrelloService;
import nz.co.trineo.trello.model.Board;

/**
 * Hello world!
 *
 */
public class App extends Application<AppConfiguration> {

	public static void main(final String[] args) throws Exception {
		new App().run(args);
	}

	private final HibernateBundle<AppConfiguration> hibernate = new HibernateBundle<AppConfiguration>(
			AccountToken.class, Client.class, ConnectedAccount.class, Credentals.class, GitProcess.class, GitTask.class,
			Backup.class, CodeCoverageResult.class, CodeCoverageWarning.class, CodeLocation.class, Organization.class,
			RunTestFailure.class, RunTestsResult.class, RunTestSuccess.class, Repository.class, Branch.class, Tag.class,
			Board.class) {
		@Override
		public DataSourceFactory getDataSourceFactory(final AppConfiguration configuration) {
			return configuration.getDataSourceFactory();
		}
	};

	@Override
	public String getName() {
		return "Salesforce App";
	}

	@Override
	public void initialize(final Bootstrap<AppConfiguration> bootstrap) {
		super.initialize(bootstrap);
		// bootstrap.addBundle(new MigrationsBundle<AppConfiguration>() {
		// @Override
		// public DataSourceFactory getDataSourceFactory(AppConfiguration
		// configuration) {
		// return configuration.getDataSourceFactory();
		// }
		// });
		bootstrap.addBundle(hibernate);
		bootstrap.addBundle(new ViewBundle<AppConfiguration>());
		bootstrap.addBundle(new AssetsBundle("/js", "/js", null, "js"));
		bootstrap.addBundle(new AssetsBundle("/css", "/css", null, "css"));
		bootstrap.addBundle(new AssetsBundle("/ui", "/ui", "ui.html", "ui"));
		bootstrap.addBundle(new QuartzBundle(hibernate.getSessionFactory()));
	}

	@Override
	public void run(final AppConfiguration configuration, final Environment environment) throws Exception {
		final SessionFactory sessionFactory = hibernate.getSessionFactory();
		final GitProcessDAO processDAO = new GitProcessDAO(sessionFactory);
		final OrganizationDAO organizationDAO = new OrganizationDAO(sessionFactory);
		final AccountDAO accountDAO = new AccountDAO(sessionFactory);
		final TestRunDAO testRunDAO = new TestRunDAO(sessionFactory);
		final BackupDAO backupDAO = new BackupDAO(sessionFactory);
		final RepoDAO repoDAO = new RepoDAO(sessionFactory);
		final ClientDAO clientDAO = new ClientDAO(sessionFactory);
		final BoardDAO boardDAO = new BoardDAO(sessionFactory);

		final GitService gService = new GitService(configuration, processDAO);
		final ClientService clientService = new ClientService(clientDAO);
		final GitHubService ghService = new GitHubService(repoDAO, configuration, accountDAO, clientService, gService);
		final SalesforceService sfService = new SalesforceService(accountDAO, organizationDAO, configuration, gService,
				testRunDAO, backupDAO, sessionFactory, clientService, ghService);
		final AccountService aService = new AccountService(accountDAO);
		final TrelloService tService = new TrelloService(configuration, accountDAO, boardDAO, clientService);

		final RepoResource gResource = new RepoResource();
		final SalesforceResource sfResource = new SalesforceResource(sfService);
		final AccountResource aResource = new AccountResource(aService);
		final TrelloResource tResource = new TrelloResource(tService);
		final StaticResource staticResource = new StaticResource();
		final ServiceResource serviceResource = new ServiceResource(aService);
		final ClientResource clientResource = new ClientResource(clientService);

		ServiceRegistry.registerService(tService);
		ServiceRegistry.registerService(sfService);
		ServiceRegistry.registerService(ghService);
		ServiceRegistry.registerService(gService);

		environment.jersey().register(gResource);
		environment.jersey().register(sfResource);
		environment.jersey().register(aResource);
		environment.jersey().register(tResource);
		environment.jersey().register(staticResource);
		environment.jersey().register(serviceResource);
		environment.jersey().register(clientResource);

		environment.admin().addTask(new RefreshBackupsTask(organizationDAO, gService, configuration, sessionFactory));
		environment.lifecycle().manage(new SalesforceScheduleManager(sessionFactory, sfService));
	}
}

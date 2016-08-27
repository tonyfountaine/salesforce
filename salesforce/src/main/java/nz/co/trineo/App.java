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
import nz.co.trineo.common.ServiceRegistry;
import nz.co.trineo.common.ServiceResource;
import nz.co.trineo.common.StaticResource;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.diff.DiffDAO;
import nz.co.trineo.diff.DiffResource;
import nz.co.trineo.diff.DiffService;
import nz.co.trineo.diff.model.Diff;
import nz.co.trineo.git.GitProcessDAO;
import nz.co.trineo.git.GitResource;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.git.model.GitTask;
import nz.co.trineo.github.GitHubResource;
import nz.co.trineo.github.GitHubService;
import nz.co.trineo.salesforce.BackupDAO;
import nz.co.trineo.salesforce.OrganizationDAO;
import nz.co.trineo.salesforce.RefreshBackupsTask;
import nz.co.trineo.salesforce.SalesforceScheduleManager;
import nz.co.trineo.salesforce.SalesforceResource;
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
import nz.co.trineo.trello.TrelloResource;
import nz.co.trineo.trello.TrelloService;

/**
 * Hello world!
 *
 */
public class App extends Application<AppConfiguration> {

	private final HibernateBundle<AppConfiguration> hibernate = new HibernateBundle<AppConfiguration>(Credentals.class,
			GitProcess.class, GitTask.class, Organization.class, ConnectedAccount.class, AccountToken.class, Diff.class,
			CodeCoverageResult.class, CodeCoverageWarning.class, CodeLocation.class, RunTestFailure.class,
			RunTestSuccess.class, RunTestsResult.class, Backup.class) {
		@Override
		public DataSourceFactory getDataSourceFactory(final AppConfiguration configuration) {
			return configuration.getDataSourceFactory();
		}
	};

	public static void main(final String[] args) throws Exception {
		new App().run(args);
	}

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
		bootstrap.addBundle(new QuartzBundle(hibernate.getSessionFactory()));
	}

	@Override
	public void run(final AppConfiguration configuration, final Environment environment) throws Exception {
		final SessionFactory sessionFactory = hibernate.getSessionFactory();
		final GitProcessDAO processDAO = new GitProcessDAO(sessionFactory);
		final OrganizationDAO organizationDAO = new OrganizationDAO(sessionFactory);
		final AccountDAO accountDAO = new AccountDAO(sessionFactory);
		final DiffDAO diffDAO = new DiffDAO(sessionFactory);
		final TestRunDAO testRunDAO = new TestRunDAO(sessionFactory);
		final BackupDAO backupDAO = new BackupDAO(sessionFactory);

		final GitService gService = new GitService(configuration, processDAO, accountDAO);
		final GitHubService ghService = new GitHubService(accountDAO, configuration);
		final SalesforceService sfService = new SalesforceService(accountDAO, organizationDAO, configuration, gService,
				testRunDAO, backupDAO, sessionFactory);
		final DiffService dService = new DiffService(diffDAO);
		final AccountService aService = new AccountService(accountDAO);
		final TrelloService tService = new TrelloService(configuration, accountDAO);

		final GitResource gResource = new GitResource(gService);
		final GitHubResource ghResource = new GitHubResource(ghService, gService);
		final SalesforceResource sfResource = new SalesforceResource(sfService, aService);
		final DiffResource dResource = new DiffResource(dService);
		final AccountResource aResource = new AccountResource(aService);
		final TrelloResource tResource = new TrelloResource(tService);
		final StaticResource staticResource = new StaticResource();
		final ServiceResource serviceResource = new ServiceResource();

		ServiceRegistry.registerService(tService);
		ServiceRegistry.registerService(sfService);
		ServiceRegistry.registerService(ghService);

		environment.jersey().register(ghResource);
		environment.jersey().register(gResource);
		environment.jersey().register(sfResource);
		environment.jersey().register(dResource);
		environment.jersey().register(aResource);
		environment.jersey().register(tResource);
		environment.jersey().register(staticResource);
		environment.jersey().register(serviceResource);

		environment.admin().addTask(new RefreshBackupsTask(organizationDAO, gService, configuration, sessionFactory));
		environment.lifecycle().manage(new SalesforceScheduleManager(sessionFactory, sfService));
	}
}

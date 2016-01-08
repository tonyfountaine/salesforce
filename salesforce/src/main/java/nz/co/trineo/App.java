package nz.co.trineo;

import org.hibernate.SessionFactory;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.AccountResource;
import nz.co.trineo.common.AccountService;
import nz.co.trineo.common.CredentalsDAO;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.diff.DiffResource;
import nz.co.trineo.diff.DiffService;
import nz.co.trineo.git.GitProcessDAO;
import nz.co.trineo.git.GitResource;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.git.model.GitTask;
import nz.co.trineo.github.GitHubResource;
import nz.co.trineo.github.GitHubService;
import nz.co.trineo.salesforce.BackupDAO;
import nz.co.trineo.salesforce.OrganizationDAO;
import nz.co.trineo.salesforce.SalesforceResource;
import nz.co.trineo.salesforce.SalesforceService;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.Organization;

/**
 * Hello world!
 *
 */
public class App extends Application<AppConfiguration> {

	private final HibernateBundle<AppConfiguration> hibernate = new HibernateBundle<AppConfiguration>(Credentals.class,
			GitProcess.class, GitTask.class, Organization.class, Backup.class, ConnectedAccount.class) {
		@Override
		public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
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
	}

	@Override
	public void run(final AppConfiguration configuration, final Environment environment) throws Exception {
		final SessionFactory sessionFactory = hibernate.getSessionFactory();
		final CredentalsDAO credentalsDAO = new CredentalsDAO(sessionFactory);
		final GitProcessDAO processDAO = new GitProcessDAO(sessionFactory);
		final OrganizationDAO organizationDAO = new OrganizationDAO(sessionFactory);
		final BackupDAO backupDAO = new BackupDAO(sessionFactory);
		final AccountDAO accountDAO = new AccountDAO(sessionFactory);

		final GitService gService = new GitService(configuration, processDAO);
		final GitResource gResource = new GitResource(gService);

		final GitHubService ghService = new GitHubService(accountDAO);
		final GitHubResource ghResource = new GitHubResource(ghService, gService);

		final SalesforceService sfService = new SalesforceService(accountDAO, organizationDAO, backupDAO,
				configuration);
		final SalesforceResource sfResource = new SalesforceResource(sfService);

		final DiffService dService = new DiffService();
		final DiffResource dResource = new DiffResource(dService);

		final AccountService aService = new AccountService(accountDAO);
		final AccountResource aResource = new AccountResource(aService);

		environment.jersey().register(ghResource);
		environment.jersey().register(gResource);
		environment.jersey().register(sfResource);
		environment.jersey().register(dResource);
		environment.jersey().register(aResource);
	}
}

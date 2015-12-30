package nz.co.trineo;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nz.co.trineo.common.CredentalsDAO;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitProcessDAO;
import nz.co.trineo.git.GitResource;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.git.model.GitTask;
import nz.co.trineo.github.GitHubResource;
import nz.co.trineo.github.GitHubService;

/**
 * Hello world!
 *
 */
public class App extends Application<AppConfiguration> {

	private final HibernateBundle<AppConfiguration> hibernate = new HibernateBundle<AppConfiguration>(Credentals.class,
			GitProcess.class, GitTask.class) {
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
		final CredentalsDAO credentalsDAO = new CredentalsDAO(hibernate.getSessionFactory());
		final GitProcessDAO processDAO = new GitProcessDAO(hibernate.getSessionFactory());
		final GitHubService ghService = new GitHubService(credentalsDAO);
		final GitService gService = new GitService(configuration, processDAO);
		final GitHubResource ghResource = new GitHubResource(ghService, gService);
		final GitResource gResource = new GitResource(gService);

		environment.jersey().register(ghResource);
		environment.jersey().register(gResource);
	}
}

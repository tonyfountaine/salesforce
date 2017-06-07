package nz.co.trineo;

import org.hibernate.SessionFactory;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.ScanningHibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nz.co.anzac.dropwizard.quartz.QuartzBundle;
import nz.co.trineo.configuration.AppConfiguration;

public class App extends Application<AppConfiguration> {

	public static void main(final String[] args) throws Exception {
		new App().run(args);
	}

	private final HibernateBundle<AppConfiguration> hibernate = new ScanningHibernateBundle<AppConfiguration>(
			"nz.co.trineo.model") {
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
		bootstrap.addBundle(new AssetsBundle("/js", "/js", null, "js"));
		bootstrap.addBundle(new AssetsBundle("/css", "/css", null, "css"));
		bootstrap.addBundle(new AssetsBundle("/ui", "/ui", "ui.html", "ui"));
		bootstrap.addBundle(new QuartzBundle(hibernate.getSessionFactory()));
	}

	@Override
	public void run(final AppConfiguration configuration, final Environment environment) throws Exception {
		final SessionFactory sessionFactory = hibernate.getSessionFactory();

		environment.jersey().register(new CustomBinder(sessionFactory, environment, configuration));

		environment.jersey().packages("nz.co.trineo.repo", "nz.co.trineo.salesforce", "nz.co.trineo.common",
				"nz.co.trineo.trello");

		environment.jersey().getResourceConfig().register(new RegisterDropwizardServices());
	}
}

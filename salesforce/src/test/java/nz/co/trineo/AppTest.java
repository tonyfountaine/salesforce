package nz.co.trineo;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheckRegistry;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import nz.co.trineo.App;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.github.GitHubResource;

/**
 * Unit test for simple App.
 */
public class AppTest {
	private final Environment environment = mock(Environment.class);
	private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
	private final App application = new App();
	private final AppConfiguration config = new AppConfiguration();
	private final DataSourceFactory factory = new DataSourceFactory();
	private final LifecycleEnvironment lifecycle = mock(LifecycleEnvironment.class);
	private final HealthCheckRegistry registry = mock(HealthCheckRegistry.class);

	@Before
	public void setup() throws Exception {
		config.setDataSourceFactory(factory);
		factory.setUrl("jdbc:h2:mem:test");
		factory.setDriverClass("org.h2.Driver");
		factory.getProperties().put("hibernate.hbm2ddl.auto", "create");
		when(environment.jersey()).thenReturn(jersey);
		when(environment.lifecycle()).thenReturn(lifecycle);
		when(environment.healthChecks()).thenReturn(registry);
	}

	@Test
	public void buildsAThingResource() throws Exception {
		application.run(config, environment);

		verify(jersey).register(isA(GitHubResource.class));
	}
}

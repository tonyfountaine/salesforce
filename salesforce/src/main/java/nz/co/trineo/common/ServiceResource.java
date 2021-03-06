package nz.co.trineo.common;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.model.ConnectedAccount;

@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceResource {

	private final AccountService aService;

	@Inject
	public ServiceResource(final AccountService aService) {
		this.aService = aService;
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/{name}/accounts")
	public Response getAccounts(final @PathParam("name") String name) {
		final List<ConnectedAccount> accounts = aService.byService(name.toLowerCase());
		return Response.ok(accounts).build();
	}

	@GET
	@Timed
	public Response listServices() {
		final Set<String> services = ServiceRegistry.listRegistedServices();
		return Response.ok(services).build();
	}
}

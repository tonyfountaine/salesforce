package nz.co.trineo.common;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceResource {

	private final ServiceRegistry registry;
	
	public ServiceResource(ServiceRegistry registry) {
		super();
		this.registry = registry;
	}

	@GET
	@Timed
	public Response listServices() {
		final Set<String> services = registry.listRegistedServices();
		return Response.ok(services).build();
	}
}

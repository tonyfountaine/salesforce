package nz.co.trineo.diff;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.diff.model.Diff;

@Path("/diff")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DiffResource {
	private final DiffService service;

	public DiffResource(DiffService service) {
		super();
		this.service = service;
	}

	@GET
	@UnitOfWork
	public Diff doDiff() throws DiffException {
		return service.doDiff();
	}
}

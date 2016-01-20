package nz.co.trineo.git;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.git.model.GitProcess;

@Path("/git")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GitResource {

	private final GitService service;

	public GitResource(final GitService service) {
		this.service = service;
	}

	@GET
	@Path("/process/{id:\\d+}")
	@Timed
	@UnitOfWork
	public Response getProcess(final @PathParam("id") int id) {
		final GitProcess process = service.getProcess(id);
		return Response.ok(process).build();
	}

	@POST
	@Timed
	@Path("/repo/{name}")
	@UnitOfWork
	public Response clone(final @PathParam("name") String name, final @FormParam("url") String cloneURL,
			final @QueryParam("acc") int accId) throws GitServiceException {
		final GitProcess process = service.clone(name, cloneURL, accId);
		return Response.ok(process).build();
	}
}

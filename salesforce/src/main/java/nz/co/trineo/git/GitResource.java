package nz.co.trineo.git;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
	public GitProcess getProcess(final @PathParam("id") int id) {
		return service.getProcess(id);
	}

	@POST
	@Timed
	@Path("/repo/{name}")
	@UnitOfWork
	public GitProcess clone(final @PathParam("name") String name, final @FormParam("url") String cloneURL)
			throws GitServiceException {
		return service.clone(name, cloneURL);
	}
}

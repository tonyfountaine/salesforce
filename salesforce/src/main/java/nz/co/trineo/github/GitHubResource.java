package nz.co.trineo.github;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.Repository;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.model.GitProcess;

@Path("/github")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubResource {

	private final GitHubService ghService;
	private final GitService gService;

	public GitHubResource(final GitHubService ghService, final GitService gService) {
		this.ghService = ghService;
		this.gService = gService;
	}

	@GET
	@Timed
	@Path("/repos")
	@UnitOfWork
	public Response getUserRepos(final @QueryParam("acc") int accId) throws Exception {
		final List<Repository> list = ghService.getRepos(accId);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/repos/{user}")
	@UnitOfWork
	public Response getUserRepos(final @PathParam("user") String user,final @QueryParam("acc") int accId) throws Exception {
		final List<Repository> list = ghService.getRepos(user, accId);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/repos/{user}/{name}")
	@UnitOfWork
	public Response getUserRepo(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final Repository repo = ghService.getRepo(user, name, accId);
		return Response.ok(repo).build();
	}

	@POST
	@Timed
	@Path("/repos/{user}/{name}")
	@UnitOfWork
	public Response cloneUserRepo(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final Repository repository = ghService.getRepo(user, name, accId);
		final String cloneURL = repository.getCloneUrl();
		final GitProcess process = gService.clone(name, cloneURL, accId);
		return Response.ok(process).build();
	}
}

package nz.co.trineo.github;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.github.model.Repository;

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
	public List<String> getRepos() throws Exception {
		return ghService.getRepos(null); //TODO get credentals
	}

	@GET
	@Timed
	@Path("/repos/{name}")
	@UnitOfWork
	public Repository getRepo(final @PathParam("name") String name) throws Exception {
		return ghService.getRepo(name,null); //TODO get credentals
	}

	@POST
	@Timed
	@Path("/repos/{name}")
	@UnitOfWork
	public GitProcess cloneRepo(final @PathParam("name") String name) throws Exception {
		final Repository repository=ghService.getRepo(name,null); //TODO get credentals
		final String cloneURL = repository.getCloneUrl();
		return gService.clone(name, cloneURL);
	}
}

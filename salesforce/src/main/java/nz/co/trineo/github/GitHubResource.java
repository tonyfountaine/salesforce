package nz.co.trineo.github;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.model.Credentals;
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
	@UnitOfWork
	public Credentals currentCredentals() {
		return ghService.currentCredentals();
	}

	@PUT
	@Timed
	@UnitOfWork
	public Credentals updateCredentals(final Credentals credentals) {
		return ghService.updateCredentals(credentals);
	}

	@POST
	@Timed
	@UnitOfWork
	public Credentals setCredentals(final @QueryParam("username") Optional<String> username,
			final @QueryParam("password") Optional<String> password,
			final @QueryParam("sessionId") Optional<String> sessionId,
			final @QueryParam("authKey") Optional<String> authKey) {
		final Credentals credentals = ghService.currentCredentals();
		credentals.setAuthKey(authKey.orNull());
		credentals.setPassword(password.orNull());
		credentals.setSessionId(sessionId.orNull());
		credentals.setUsername(username.orNull());
		return ghService.updateCredentals(credentals);
	}

	@GET
	@Timed
	@Path("/repos")
	@UnitOfWork
	public List<String> getRepos() throws Exception {
		return ghService.getRepos();
	}

	@GET
	@Timed
	@Path("/repos/{name}")
	@UnitOfWork
	public Repository getRepo(final @PathParam("name") String name) throws Exception {
		return ghService.getRepo(name);
	}

	@POST
	@Timed
	@Path("/repos/{name}")
	@UnitOfWork
	public GitProcess cloneRepo(final @PathParam("name") String name) throws Exception {
		final Repository repository=ghService.getRepo(name);
		final String cloneURL = repository.getCloneUrl();
		return gService.clone(name, cloneURL);
	}
}

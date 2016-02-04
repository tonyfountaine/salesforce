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
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryTag;

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
	public Response getRepos(final @QueryParam("acc") int accId) throws Exception {
		final List<Repository> list = ghService.getRepos(accId);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/{user}/repos")
	@UnitOfWork
	public Response getUserRepos(final @PathParam("user") String user, final @QueryParam("acc") int accId)
			throws Exception {
		final List<Repository> list = ghService.getRepos(user, accId);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/repos/{user}/{name}")
	@UnitOfWork
	public Response getRepo(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final Repository repo = ghService.getRepo(user, name, accId);
		return Response.ok(repo).build();
	}

	@GET
	@Timed
	@Path("/repos/{user}/{name}/branches")
	@UnitOfWork
	public Response getBranches(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryBranch> branches = ghService.getBranches(user, name, accId);
		return Response.ok(branches).build();
	}

	@GET
	@Timed
	@Path("/repos/{user}/{name}/tags")
	@UnitOfWork
	public Response getTags(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryTag> tags = ghService.getTags(user, name, accId);
		return Response.ok(tags).build();
	}

	@GET
	@Timed
	@Path("/repos/{user}/{name}/commits")
	@UnitOfWork
	public Response getCommits(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryCommit> commits = ghService.getCommits(user, name, accId);
		return Response.ok(commits).build();
	}

	@GET
	@Timed
	@Path("/repos/{user}/{name}/commits/{sha1}")
	@UnitOfWork
	public Response getCommit(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId, final @PathParam("sha1") String sha1) throws Exception {
		final RepositoryCommit commit = ghService.getCommit(user, name, accId, sha1);
		return Response.ok(commit).build();
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

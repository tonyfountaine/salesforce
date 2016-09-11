package nz.co.trineo.github;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.RepositoryCommit;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.AccountService;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.github.model.Repository;
import nz.co.trineo.github.views.RepoView;
import nz.co.trineo.github.views.ReposView;

@Path("/github")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubResource {

	private final GitHubService ghService;
	private final AccountService accountService;

	public GitHubResource(final GitHubService ghService, final AccountService accountService) {
		this.ghService = ghService;
		this.accountService = accountService;
	}

	@GET
	@Timed
	@Path("/repos")
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response getHTMLRepos() throws GitHubServiceException {
		final List<Repository> list = ghService.getRepos();
		final List<ConnectedAccount> accounts = accountService.byService(ghService.getName().toLowerCase());
		final ReposView view = new ReposView(list, accounts);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}")
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response getRepo(final @PathParam("id") int id) throws GitHubServiceException {
		final Repository repo = ghService.getRepo(id);
		final RepoView view = new RepoView(repo);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/branches")
	@UnitOfWork
	public Response getBranches(final @PathParam("id") int id) throws GitHubServiceException {
		final List<String> branches = ghService.getBranches(id);
		return Response.ok(branches).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/tags")
	@UnitOfWork
	public Response getTags(final @PathParam("id") int id) throws GitHubServiceException {
		final List<String> tags = ghService.getTags(id);
		return Response.ok(tags).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/commits")
	@UnitOfWork
	public Response getCommits(final @PathParam("id") int id) throws GitHubServiceException {
		final List<String> commits = ghService.getCommits(id);
		return Response.ok(commits).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/commits/{sha1}")
	@UnitOfWork
	public Response getCommit(final @PathParam("id") int id, final @PathParam("sha1") String sha1)
			throws GitHubServiceException {
		final RepositoryCommit commit = ghService.getCommit(id, sha1);
		return Response.ok(commit).build();
	}

	@POST
	@Timed
	@Path("/repos")
	@UnitOfWork
	public Response cloneRepo(final @QueryParam("acc") int accId, final @QueryParam("url") String url)
			throws GitHubServiceException {
		final Repository repo = ghService.createRepo(url, accId);
		return Response.ok(repo).build();
	}

	@DELETE
	@Timed
	@Path("/repos/{id}")
	@UnitOfWork
	public void deleteRepo(final @PathParam("id") int id) {
		ghService.deleteRepo(id);
	}
}

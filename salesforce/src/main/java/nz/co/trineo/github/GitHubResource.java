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

import org.eclipse.egit.github.core.RepositoryCommit;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.github.model.Repository;
import nz.co.trineo.github.views.ReposView;

@Path("/github")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubResource {

	private final GitHubService ghService;

	public GitHubResource(final GitHubService ghService) {
		this.ghService = ghService;
	}

	@GET
	@Timed
	@Path("/repos")
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response getHTMLRepos() throws GitHubServiceException {
		final List<Repository> list = ghService.getRepos();
		final ReposView view = new ReposView(list);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@Path("/repos/{name}")
	@UnitOfWork
	public Response getRepo(final @PathParam("name") String name) throws GitHubServiceException {
		final Repository repo = ghService.getRepo(name);
		return Response.ok(repo).build();
	}

	@GET
	@Timed
	@Path("/repos/{name}/branches")
	@UnitOfWork
	public Response getBranches(final @PathParam("name") String name) throws GitHubServiceException {
		final List<String> branches = ghService.getBranches(name);
		return Response.ok(branches).build();
	}

	@GET
	@Timed
	@Path("/repos/{name}/tags")
	@UnitOfWork
	public Response getTags(final @PathParam("name") String name) throws GitHubServiceException {
		final List<String> tags = ghService.getTags(name);
		return Response.ok(tags).build();
	}

	@GET
	@Timed
	@Path("/repos/{name}/commits")
	@UnitOfWork
	public Response getCommits(final @PathParam("name") String name) throws GitHubServiceException {
		final List<String> commits = ghService.getCommits(name);
		return Response.ok(commits).build();
	}

	@GET
	@Timed
	@Path("/repos/{name}/commits/{sha1}")
	@UnitOfWork
	public Response getCommit(final @PathParam("name") String name, final @PathParam("sha1") String sha1)
			throws GitHubServiceException {
		final RepositoryCommit commit = ghService.getCommit(name, sha1);
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
}

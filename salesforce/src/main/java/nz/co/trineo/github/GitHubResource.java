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
import nz.co.trineo.github.views.ReposView;

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
	@Produces(MediaType.TEXT_HTML)
	public Response getHTMLRepos(final @QueryParam("acc") int accId) throws Exception {
		final List<Repository> list = ghService.getRepos(accId);
		final ReposView view = new ReposView(list);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@Path("/users/{user}/repos")
	@UnitOfWork
	public Response getUserRepos(final @PathParam("user") String user, final @QueryParam("acc") int accId)
			throws Exception {
		final List<Repository> list = ghService.getRepos(user, accId);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/users/{user}/repos/{name}")
	@UnitOfWork
	public Response getUserRepo(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final Repository repo = ghService.getRepo(user, name, accId);
		return Response.ok(repo).build();
	}

	@GET
	@Timed
	@Path("/users/{user}/repos/{name}/branches")
	@UnitOfWork
	public Response getUserBranches(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryBranch> branches = ghService.getBranches(user, name, accId);
		return Response.ok(branches).build();
	}

	@GET
	@Timed
	@Path("/users/{user}/repos/{name}/tags")
	@UnitOfWork
	public Response getUserTags(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryTag> tags = ghService.getTags(user, name, accId);
		return Response.ok(tags).build();
	}

	@GET
	@Timed
	@Path("/users/{user}/repos/{name}/commits")
	@UnitOfWork
	public Response getUserCommits(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryCommit> commits = ghService.getCommits(user, name, accId);
		return Response.ok(commits).build();
	}

	@GET
	@Timed
	@Path("/users/{user}/repos/{name}/commits/{sha1}")
	@UnitOfWork
	public Response getUserCommit(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId, final @PathParam("sha1") String sha1) throws Exception {
		final RepositoryCommit commit = ghService.getCommit(user, name, accId, sha1);
		return Response.ok(commit).build();
	}

	@POST
	@Timed
	@Path("/users/{user}/repos/{name}")
	@UnitOfWork
	public Response cloneUserRepo(final @PathParam("user") String user, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final Repository repository = ghService.getRepo(user, name, accId);
		final String cloneURL = repository.getCloneUrl();
		final GitProcess process = gService.clone(name, cloneURL, accId);
		return Response.ok(process).build();
	}

	@GET
	@Timed
	@Path("/orgs/{org}/repos")
	@UnitOfWork
	public Response getOrgRepos(final @PathParam("org") String org, final @QueryParam("acc") int accId)
			throws Exception {
		final List<Repository> list = ghService.getOrgRepos(org, accId);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/orgs/{org}/repos/{name}")
	@UnitOfWork
	public Response getOrgRepo(final @PathParam("org") String org, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final Repository repo = ghService.getRepo(org, name, accId);
		return Response.ok(repo).build();
	}

	@GET
	@Timed
	@Path("/orgs/{org}/repos/{name}/branches")
	@UnitOfWork
	public Response getOrgBranches(final @PathParam("org") String org, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryBranch> branches = ghService.getBranches(org, name, accId);
		return Response.ok(branches).build();
	}

	@GET
	@Timed
	@Path("/orgs/{org}/repos/{name}/tags")
	@UnitOfWork
	public Response getOrgTags(final @PathParam("org") String org, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryTag> tags = ghService.getTags(org, name, accId);
		return Response.ok(tags).build();
	}

	@GET
	@Timed
	@Path("/orgs/{org}/repos/{name}/commits")
	@UnitOfWork
	public Response getOrgCommits(final @PathParam("org") String org, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final List<RepositoryCommit> commits = ghService.getCommits(org, name, accId);
		return Response.ok(commits).build();
	}

	@GET
	@Timed
	@Path("/orgs/{org}/repos/{name}/commits/{sha1}")
	@UnitOfWork
	public Response getOrgCommit(final @PathParam("org") String org, final @PathParam("name") String name,
			final @QueryParam("acc") int accId, final @PathParam("sha1") String sha1) throws Exception {
		final RepositoryCommit commit = ghService.getCommit(org, name, accId, sha1);
		return Response.ok(commit).build();
	}

	@POST
	@Timed
	@Path("/orgs/{org}/repos/{name}")
	@UnitOfWork
	public Response cloneOrgRepo(final @PathParam("org") String org, final @PathParam("name") String name,
			final @QueryParam("acc") int accId) throws Exception {
		final Repository repository = ghService.getRepo(org, name, accId);
		final String cloneURL = repository.getCloneUrl();
		final GitProcess process = gService.clone(name, cloneURL, accId);
		return Response.ok(process).build();
	}
}

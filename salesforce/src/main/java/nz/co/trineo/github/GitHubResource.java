package nz.co.trineo.github;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import nz.co.trineo.common.ClientService;
import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.github.model.Branch;
import nz.co.trineo.github.model.Repository;
import nz.co.trineo.github.model.Tag;
import nz.co.trineo.github.views.CompareView;
import nz.co.trineo.github.views.RepoView;
import nz.co.trineo.github.views.ReposView;
import nz.co.trineo.salesforce.SalesforceException;
import nz.co.trineo.salesforce.model.TreeNode;

@Path("/github")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubResource {

	private final GitHubService ghService;
	private final AccountService accountService;
	private final ClientService clientService;

	public GitHubResource(final GitHubService ghService, final AccountService accountService,
			final ClientService clientService) {
		this.ghService = ghService;
		this.accountService = accountService;
		this.clientService = clientService;
	}

	@GET
	@Timed
	@Path("/repos")
	@UnitOfWork
	public Response getRepos() throws GitHubServiceException {
		final List<Repository> list = ghService.getRepos();
		final List<ConnectedAccount> accounts = accountService.byService(ghService.getName().toLowerCase());
		final ReposView view = new ReposView(list, accounts);
		return Response.ok(view).build();
	}

	@PUT
	@Path("/repos")
	@Timed
	@UnitOfWork
	public Response updateRepo(final Repository repo) throws SalesforceException {
		final Repository updatedRepo = ghService.updateRepo(repo);
		return Response.ok(updatedRepo).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}")
	@UnitOfWork
	public Response getRepo(final @PathParam("id") int id) throws GitHubServiceException {
		final Repository repo = ghService.getRepo(id);
		final List<Branch> branches = repo.getBranches();
		System.out.println(branches);
		final List<Tag> tags = repo.getTags();
		final List<Client> clients = clientService.list();
		final RepoView view = new RepoView(repo, branches, tags, clients);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/branches")
	@UnitOfWork
	public Response getBranches(final @PathParam("id") int id) throws GitHubServiceException {
		final List<Branch> branches = ghService.getBranches(id);
		return Response.ok(branches).build();
	}

	@GET
	@Timed
	@Path("/branches/{id}/compare/{compareId}")
	@UnitOfWork
	public Response compareBranches(final @PathParam("id") long id, final @PathParam("compareId") long compareId)
			throws GitHubServiceException {
		final List<GitDiff> list = ghService.diffBranches(id, compareId);
		final TreeNode diffTree = ghService.getDiffTree(list);
		final CompareView view = new CompareView(list, diffTree);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/tags")
	@UnitOfWork
	public Response getTags(final @PathParam("id") int id) throws GitHubServiceException {
		final List<Tag> tags = ghService.getTags(id);
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

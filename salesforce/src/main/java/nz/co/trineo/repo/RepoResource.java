package nz.co.trineo.repo;

import java.util.ArrayList;
import java.util.Comparator;
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
import nz.co.trineo.common.ServiceRegistry;
import nz.co.trineo.model.Branch;
import nz.co.trineo.model.GitDiff;
import nz.co.trineo.model.Repository;
import nz.co.trineo.model.Tag;
import nz.co.trineo.repo.view.BranchView;
import nz.co.trineo.repo.view.RepositoryView;
import nz.co.trineo.repo.view.TagView;

@Path("/{type:git|github|bitbucket}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RepoResource {

	private RepoService getService(final String type) {
		return (RepoService) ServiceRegistry.getService(type);
	}

	@GET
	@Timed
	@Path("/repos")
	@UnitOfWork
	public Response getRepos(final @PathParam("type") String type) throws RepoServiceException {
		final List<Repository> list = getService(type).getRepos();
		final List<RepositoryView> repos = new ArrayList<>();
		list.forEach(r -> {
			repos.add(new RepositoryView(r));
		});
		return Response.ok(repos).build();
	}

	@POST
	@Timed
	@Path("/repos")
	@UnitOfWork
	public Response cloneRepo(final @PathParam("type") String type, final @QueryParam("acc") int accId,
			final @QueryParam("url") String url) throws RepoServiceException {
		final Repository repo = getService(type).createRepo(url, accId);
		final RepositoryView view = new RepositoryView(repo);
		return Response.ok(view).build();
	}

	@PUT
	@Path("/repos")
	@Timed
	@UnitOfWork
	public Response updateRepo(final @PathParam("type") String type, final Repository repo)
			throws RepoServiceException {
		final Repository updatedRepo = getService(type).updateRepo(repo);
		final RepositoryView view = new RepositoryView(updatedRepo);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}")
	@UnitOfWork
	public Response getRepo(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		final Repository repo = getService(type).getRepo(id);
		final RepositoryView view = new RepositoryView(repo);
		return Response.ok(view).build();
	}

	@DELETE
	@Timed
	@Path("/repos/{id}")
	@UnitOfWork
	public void deleteRepo(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		getService(type).deleteRepo(id);
	}

	@POST
	@Timed
	@Path("/repos/{id}/pull")
	@UnitOfWork
	public Response pullRepo(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		getService(type).pull(id);
		return Response.ok().build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/branches")
	@UnitOfWork
	public Response getBranches(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		final List<Branch> branches = getService(type).getBranches(id);
		final List<BranchView> list = toBranchView(branches);
		return Response.ok(list).build();
	}

	@POST
	@Timed
	@Path("/repos/{id}/branches")
	@UnitOfWork
	public Response updateBranches(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		final List<Branch> branches = getService(type).updateBranches(id);
		final List<BranchView> list = toBranchView(branches);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/commits/{sha1}")
	@UnitOfWork
	public Response getCommit(final @PathParam("type") String type, final @PathParam("id") int id,
			final @PathParam("sha1") String sha1) throws RepoServiceException {
		final RepositoryCommit commit = getService(type).getCommit(id, sha1);
		return Response.ok(commit).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/commits")
	@UnitOfWork
	public Response getCommits(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		final List<String> commits = getService(type).getCommits(id);
		return Response.ok(commits).build();
	}

	@GET
	@Timed
	@Path("/repos/{id}/tags")
	@UnitOfWork
	public Response getTags(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		final List<Tag> tags = getService(type).getTags(id);
		final List<TagView> list = new ArrayList<>();
		tags.forEach(t -> {
			list.add(new TagView(t));
		});
		return Response.ok(list).build();
	}

	@POST
	@Timed
	@Path("/repos/{id}/tags")
	@UnitOfWork
	public Response updateTags(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		final List<Tag> tags = getService(type).updateTags(id);
		final List<TagView> list = new ArrayList<>();
		tags.forEach(t -> {
			list.add(new TagView(t));
		});
		return Response.ok(list).build();
	}

	@POST
	@Timed
	@Path("/branches/{id}")
	@UnitOfWork
	public Response createBranch(final @PathParam("type") String type, final @PathParam("id") int id,
			final @QueryParam("name") String branchName) throws RepoServiceException {
		final Branch branch = getService(type).createBranch(id, branchName);
		final BranchView view = new BranchView(branch);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@Path("/branches/{id}/compare/{compareId}")
	@UnitOfWork
	public Response compareBranches(final @PathParam("type") String type, final @PathParam("id") long id,
			final @PathParam("compareId") long compareId) throws RepoServiceException {
		final List<GitDiff> list = getService(type).diffBranches(id, compareId);
		return Response.ok(list).build();
	}

	@POST
	@Timed
	@Path("/branches/{id}/checkout")
	@UnitOfWork
	public Response checkoutBranch(final @PathParam("type") String type, final @PathParam("id") int id)
			throws RepoServiceException {
		getService(type).checkout(id);
		return Response.ok().build();
	}

	private List<BranchView> toBranchView(final List<Branch> branches) {
		final List<BranchView> list = new ArrayList<>();
		branches.forEach(b -> {
			list.add(new BranchView(b));
		});
		list.sort(new Comparator<BranchView>() {
			@Override
			public int compare(BranchView o1, BranchView o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return list;
	}
}

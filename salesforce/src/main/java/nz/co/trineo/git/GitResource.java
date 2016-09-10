package nz.co.trineo.git;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.transport.PushResult;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.git.model.GitRepo;

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
	public Response getProcess(final @PathParam("id") int id) {
		final GitProcess process = service.getProcess(id);
		return Response.ok(process).build();
	}

	// @POST
	// @Timed
	// @Path("/repo/{name}")
	// @UnitOfWork
	// public Response clone(final @PathParam("name") String name, final @FormParam("url") String cloneURL,
	// final @QueryParam("acc") int accId) throws GitServiceException {
	// final GitProcess process = service.clone(name, cloneURL, accId);
	// return Response.ok(process).build();
	// }

	@GET
	@Timed
	@UnitOfWork
	@Path("/repo")
	public Response listRepos() throws GitServiceException {
		final List<GitRepo> listRepos = service.listRepos();
		return Response.ok(listRepos).build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}")
	public Response createRepo(final @PathParam("name") String name) throws GitServiceException {
		service.createRepo(name);
		return Response.ok().build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/commit")
	public Response commit(final @PathParam("name") String name, final @FormParam("message") String message)
			throws GitServiceException {
		service.commit(name, message);
		return Response.ok().build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/pull")
	public Response pull(final @PathParam("name") String name) throws GitServiceException {
		final PullResult result = service.pull(name);
		return Response.ok(result).build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/push")
	public Response push(final @PathParam("name") String name) throws GitServiceException {
		final Iterable<PushResult> push = service.push(name);
		return Response.ok(push).build();
	}

	@PUT
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/creds")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response credentals(final @PathParam("name") String name, final @FormParam("username") String username,
			final @FormParam("password") String password) throws GitServiceException {
		service.credentals(name, username, password);
		return Response.ok().build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/tags")
	public Response getTags(final @PathParam("name") String name) throws GitServiceException {
		final List<String> tags = service.getTags(name);
		return Response.ok(tags).build();
	}

	@DELETE
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/tags/{tag}")
	public Response removeTag(final @PathParam("name") String name, final @PathParam("tag") String tag)
			throws GitServiceException {
		final List<String> list = service.removeTag(name, tag);
		return Response.ok(list).build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/tags/{tag}")
	public Response createTag(final @PathParam("name") String name, final @PathParam("tag") String tag)
			throws GitServiceException {
		service.tag(name, tag);
		return Response.ok().build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/branches")
	public Response listBranches(final @PathParam("name") String name) throws GitServiceException {
		final List<String> branches = service.branches(name);
		return Response.ok(branches).build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/branches/{branch}")
	public Response checkout(final @PathParam("name") String name, final @PathParam("branch") String branch)
			throws GitServiceException {
		service.checkout(name, branch);
		return Response.ok().build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/diff/{first}/{second}")
	public Response diff(final @PathParam("name") String name, final @PathParam("first") String first,
			final @PathParam("second") String second) throws GitServiceException {
		final List<GitDiff> list = service.diff(name, first, second);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/remote")
	public Response getRemote(final @PathParam("name") String name) throws GitServiceException {
		final String remote = service.getRemote(name);
		return Response.ok(remote).build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/remote/fetch")
	public Response fetch(final @PathParam("name") String name) throws GitServiceException {
		service.fetchRemote(name);
		return Response.ok().build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/remote/diff")
	public Response diffRepos(final @PathParam("nameA") String name) throws GitServiceException {
		final List<GitDiff> list = service.diffRepos(name);
		return Response.ok(list).build();
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/repo/{nameA}/remote/{nameB}")
	public Response setRemote(final @PathParam("nameA") String nameA, final @PathParam("nameB") String nameB)
			throws GitServiceException {
		service.addRemote(nameA, nameB);
		return Response.ok().build();
	}

	@DELETE
	@Timed
	@UnitOfWork
	@Path("/repo/{name}/remote")
	public Response removeRemote(final @PathParam("name") String name) throws GitServiceException {
		service.removeRemote(name);
		return Response.ok().build();
	}
}

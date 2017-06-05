package nz.co.trineo.common;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.model.Client;
import nz.co.trineo.model.Organization;
import nz.co.trineo.model.Repository;
import nz.co.trineo.repo.view.OrganizationView;
import nz.co.trineo.repo.view.RepositoryView;
import nz.co.trineo.trello.model.Board;

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource {

	private final ClientService clientService;

	@Context
	UriInfo uriInfo;

	@Inject
	public ClientResource(final ClientService clientService) {
		this.clientService = clientService;
	}

	@POST
	@Timed
	@UnitOfWork
	public Client create(final @QueryParam("name") String name) {
		final Client client = new Client();
		client.setName(name);
		return clientService.create(client);
	}

	@DELETE
	@Timed
	@UnitOfWork
	@Path("/{id}")
	public void delete(final @PathParam("id") long id) {
		clientService.delete(id);
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/{id}/boards")
	public Response getBoards(final @PathParam("id") long id) {
		final List<Board> boards = clientService.read(id).getBoards();
		boards.size(); // lazy loading
		return Response.ok(boards).build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/{id}/organizations")
	public Response getOrganizations(final @PathParam("id") long id) {
		final List<Organization> organizations = clientService.read(id).getOrganizations();
		final List<OrganizationView> list = new ArrayList<>();
		organizations.forEach(o -> {
			list.add(new OrganizationView(o));
		});
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/{id}/repos")
	public Response getRepos(final @PathParam("id") long id) {
		final List<Repository> repositories = clientService.read(id).getRepositories();
		final List<RepositoryView> list = new ArrayList<>();
		repositories.forEach(r -> {
			list.add(new RepositoryView(r));
		});
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@UnitOfWork
	public Response listAccounts() {
		final List<Client> list = clientService.list();
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/{id}")
	public Client read(final @PathParam("id") int id) {
		return clientService.read(id);
	}

	@PUT
	@Timed
	@UnitOfWork
	public Client update(final Client account) {
		return clientService.update(account);
	}
}

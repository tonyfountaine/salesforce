package nz.co.trineo.common;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.views.ClientsView;
import nz.co.trineo.github.model.Repository;
import nz.co.trineo.salesforce.model.Organization;

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource {

	private static final Log log = LogFactory.getLog(ClientResource.class);

	private final ClientService clientService;

	@Context
	UriInfo uriInfo;

	public ClientResource(final ClientService clientService) {
		this.clientService = clientService;
	}

	@GET
	@Timed
	@UnitOfWork
	public Response listAccounts() {
		List<Client> list = clientService.list();
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public ClientsView listHTML() {
		final List<Client> clients = clientService.list();
		log.debug(clients);
		return new ClientsView(clients);
	}

	@POST
	@Timed
	@UnitOfWork
	public Client create(final @QueryParam("name") String name) {
		final Client client = new Client();
		client.setName(name);
		return clientService.create(client);
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
	@Path("/{id}/organizations")
	public Response getOrganizations(final @PathParam("id") long id) {
		List<Organization> organizations = clientService.read(id).getOrganizations();
		organizations.size();
		return Response.ok(organizations).build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/{id}/repos")
	public Response getRepos(final @PathParam("id") long id) {
		List<Repository> repositories = clientService.read(id).getRepositories();
		repositories.size();
		return Response.ok(repositories).build();
	}
}

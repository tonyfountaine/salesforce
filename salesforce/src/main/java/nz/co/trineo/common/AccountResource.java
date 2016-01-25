package nz.co.trineo.common;

import static javax.ws.rs.core.UriBuilder.fromResource;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.common.views.AccountsView;
import nz.co.trineo.common.views.SuccessView;

@Path("/accounts")
@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

	private static final Log log = LogFactory.getLog(AccountResource.class);

	private final AccountService accountService;

	@Context
	UriInfo uriInfo;

	public AccountResource(AccountService accountService) {
		this.accountService = accountService;
	}

	// @GET
	// @Timed
	// @UnitOfWork
	// public List<ConnectedAccount> listAccounts() {
	// return accountService.list();
	// }

	@GET
	@Timed
	@UnitOfWork
	public AccountsView listHTML() {
		final List<ConnectedAccount> accounts = accountService.list();
		return new AccountsView(accounts, ServiceRegistry.listRegistedServices());
	}

	@POST
	@Timed
	@UnitOfWork
	public ConnectedAccount create(final ConnectedAccount account) {
		return accountService.create(account);
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/{id}")
	public ConnectedAccount read(final @PathParam("id") int id) {
		return accountService.read(id);
	}

	@PUT
	@Timed
	@UnitOfWork
	public ConnectedAccount update(final ConnectedAccount account) {
		return accountService.update(account);
	}

	@DELETE
	@Timed
	@UnitOfWork
	@Path("/{id}")
	public void delete(final @PathParam("id") int id) {
		accountService.delete(id);
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/oauth")
	public Response startConnect(final @QueryParam("service") String serviceName, final @QueryParam("name") String name) {
		final URI uri = getRedirectUri(serviceName);
		final ConnectedAccount account = new ConnectedAccount();
		account.setName(name);
		account.setService(serviceName);
		accountService.create(account);
		final URI url = accountService.getAuthorizeURIForService(account, uri);
		log.info(url);
		return Response.temporaryRedirect(url).build();
	}

	private URI getRedirectUri(final String serviceName) {
		return uriInfo.resolve(fromResource(getClass()).path(getClass(), "finishConnect").build(serviceName));
	}

	@POST
	@Timed
	@UnitOfWork
	@Path("/oauth/{service}/callback")
	public SuccessView finishConnect(final @PathParam("service") String serviceName,
			final @QueryParam("code") String code, final @QueryParam("state") String state) throws IOException {
		log.info("processing post");
		return extracted(serviceName, code, state);
	}

	private SuccessView extracted(final String serviceName, final String code, final String state) throws IOException {
		final URI uri = getRedirectUri(serviceName);

		accountService.getAccessToken(code, state, uri);

		return new SuccessView();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/oauth/{service}/callback")
	public SuccessView getFinishConnect(final @PathParam("service") String serviceName,
			final @QueryParam("code") String code, final @QueryParam("state") String state) throws IOException {
		log.info("processing get");
		return extracted(serviceName, code, state);
	}
}

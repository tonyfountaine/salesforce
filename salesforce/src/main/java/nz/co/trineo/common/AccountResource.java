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
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.common.views.AccountsView;

@Path("/accounts")
@Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {
	private final AccountService accountService;

	public AccountResource(AccountService accountService) {
		this.accountService = accountService;
	}

//	@GET
//	@Timed
//	@UnitOfWork
//	public List<ConnectedAccount> listAccounts() {
//		return accountService.list();
//	}
	
	@GET
	@Timed
	@UnitOfWork
	public AccountsView listHTML() {
		final List<ConnectedAccount> accounts = accountService.list();
		return new AccountsView(accounts);
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
}

package nz.co.trineo.salesforce;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.model.Credentals;

@Path("/git")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalesforceResource {

	private final SalesforceService salesforceService;

	public SalesforceResource(SalesforceService salesforceService) {
		super();
		this.salesforceService = salesforceService;
	}

	@GET
	@Timed
	@UnitOfWork
	public Credentals currentCredentals() {
		return salesforceService.currentCredentals();
	}

	@PUT
	@Timed
	@UnitOfWork
	public Credentals updateCredentals(final Credentals credentals) {
		return salesforceService.updateCredentals(credentals);
	}

	@POST
	@Timed
	@UnitOfWork
	public Credentals setCredentals(final @QueryParam("username") Optional<String> username,
			final @QueryParam("password") Optional<String> password,
			final @QueryParam("sessionId") Optional<String> sessionId,
			final @QueryParam("authKey") Optional<String> authKey) {
		final Credentals credentals = salesforceService.currentCredentals();
		credentals.setAuthKey(authKey.orNull());
		credentals.setPassword(password.orNull());
		credentals.setSessionId(sessionId.orNull());
		credentals.setUsername(username.orNull());
		return salesforceService.updateCredentals(credentals);
	}
	
	
}

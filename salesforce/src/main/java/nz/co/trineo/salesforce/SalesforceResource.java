package nz.co.trineo.salesforce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.sforce.soap.tooling.RunTestsResult;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.SalesforceRequest;

@Path("/sf")
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

	@POST
	@Path("/metadata")
	@Timed
	@UnitOfWork
	public void getMetadata(final SalesforceRequest request) throws SalesforceException {
		final String endpoint = getOrgurl(request);
		salesforceService.downloadAllMetadata(endpoint);
	}

	@POST
	@Path("/backup")
	@Timed
	@UnitOfWork
	public Backup createBackup(final SalesforceRequest request) throws SalesforceException {
		final String endpoint = getOrgurl(request);
		return salesforceService.createBackup(endpoint);
	}

	private String getOrgurl(final SalesforceRequest request) {
		final String orgURL;
		switch (request.getEnvironment()) {
		case SANDBOX:
			orgURL = "https://test.salesforce.com"; // "/services/Soap/u/35.0"
			break;
		case OTHER:
			orgURL = request.getOrgUrl();
			break;
		case DEVELOPER:
		case PRODUCTION:
		default:
			orgURL = "https://login.salesforce.com"; // "/services/Soap/u/35.0"
			break;
		}
		return orgURL;
	}

	@GET
	@Path("/backups")
	@Timed
	@UnitOfWork
	public List<Backup> listBackups() {
		return salesforceService.listBackups();
	}

	@GET
	@Path("/backups/{date}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getBackup(final @PathParam("date") String date) throws SalesforceException {
		final InputStream in = salesforceService.downloadBackup(date);
		final StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream output) throws IOException, WebApplicationException {
				try {
					IOUtils.copy(in, output);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + date + ".zip").build();
	}

	@POST
	@Path("/tests")
	@Timed
	@UnitOfWork
	public void runTests(final SalesforceRequest request) throws SalesforceException {
		final String endpoint = getOrgurl(request);
		RunTestsResult runTests = salesforceService.runTests(endpoint, null);
		System.out.println(runTests);
	}
}

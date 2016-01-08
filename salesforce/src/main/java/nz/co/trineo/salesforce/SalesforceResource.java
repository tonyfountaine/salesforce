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
import com.sforce.soap.apex.RunTestsResult;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.Environment;
import nz.co.trineo.salesforce.model.Organization;
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

	@POST
	@Path("/org")
	@Timed
	@UnitOfWork
	public Organization getOrg(final @QueryParam("environment") Environment environment, final Credentals credentals)
			throws SalesforceException {
		final SalesforceRequest request = new SalesforceRequest();
		request.setEnvironment(environment);
		final String endpoint = getOrgurl(request);
		return salesforceService.getOrg(endpoint, credentals);
	}

	@GET
	@Path("/orgs/{id}")
	@Timed
	@UnitOfWork
	public Organization getOrg(final @PathParam("id") String id) throws SalesforceException {
		return salesforceService.getOrg(id);
	}

	@POST
	@Path("/orgs/{id}/metadata")
	@Timed
	@UnitOfWork
	public void getMetadata(final @PathParam("id") String id, final SalesforceRequest request)
			throws SalesforceException {
		final String endpoint = getOrgurl(request);
		salesforceService.downloadAllMetadata(id, endpoint, null);// TODO get
																	// credentals
	}

	@POST
	@Path("/orgs/{id}/backup")
	@Timed
	@UnitOfWork
	public Backup createBackup(final @PathParam("id") String id, final SalesforceRequest request)
			throws SalesforceException {
		final Organization organization = salesforceService.getOrg(id);
		final String endpoint = getOrgurl(request);
		return salesforceService.createBackup(id, endpoint, null);// TODO get
																	// credentals
	}

	private String getOrgurl(final SalesforceRequest request) {
		final String orgURL;
		switch (request.getEnvironment()) {
		case SANDBOX:
			orgURL = "https://test.salesforce.com";
			break;
		case OTHER:
			orgURL = request.getOrgUrl();
			break;
		case DEVELOPER:
		case PRODUCTION:
		default:
			orgURL = "https://login.salesforce.com";
			break;
		}
		return orgURL;
	}

	@GET
	@Path("/orgs/{id}/backups")
	@Timed
	@UnitOfWork
	public List<Backup> listBackups(final @PathParam("id") String id) {
		return salesforceService.listBackups(id);
	}

	@GET
	@Path("/orgs/{id}/backups/{date}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getBackup(final @PathParam("id") String id, final @PathParam("date") String date)
			throws SalesforceException {
		final InputStream in = salesforceService.downloadBackup(id, date);
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
	@Path("/orgs/{id}/tests")
	@Timed
	@UnitOfWork
	public RunTestsResult runTests(final @PathParam("id") String id, final SalesforceRequest request)
			throws SalesforceException {
		final String endpoint = getOrgurl(request);
		RunTestsResult runTests = salesforceService.runTests(id, endpoint, null, null);// TODO
																						// get
																						// credentals
		return runTests;
	}
}

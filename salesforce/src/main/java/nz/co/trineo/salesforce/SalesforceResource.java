package nz.co.trineo.salesforce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;

import com.codahale.metrics.annotation.Timed;
import com.sforce.soap.apex.RunTestsResult;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.Environment;
import nz.co.trineo.salesforce.model.Organization;

@Path("/sf")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalesforceResource {

	private final SalesforceService salesforceService;

	@Context
	private Request request;

	public SalesforceResource(SalesforceService salesforceService) {
		super();
		this.salesforceService = salesforceService;
	}

	@POST
	@Path("/refresh")
	@Timed
	@UnitOfWork
	public Response refresh() {
		salesforceService.updateBackupsFromFilesystem();
		return Response.noContent().build();
	}

	@POST
	@Path("/orgs")
	@Timed
	@UnitOfWork
	public Response addOrg(final @QueryParam("environment") Environment environment, final @QueryParam("acc") int accId)
			throws SalesforceException {
		final String endpoint = getOrgurl(environment);
		final Organization org = salesforceService.addOrg(endpoint, accId);
		return Response.created(UriBuilder.fromMethod(getClass(), "getOrg").build(org.getId())).entity(org).build();
	}

	@GET
	@Path("/orgs/{id}")
	@Timed
	@UnitOfWork
	public Response getOrg(final @PathParam("id") String id) throws SalesforceException {
		final Organization org = salesforceService.getOrg(id);
		return Response.ok(org).build();
	}

	@POST
	@Path("/orgs/{id}/metadata")
	@Timed
	@UnitOfWork
	public Response getMetadata(final @PathParam("id") String id, final @QueryParam("acc") int accId)
			throws SalesforceException {
		salesforceService.downloadAllMetadata(id, accId);
		return Response.ok().build();
	}

	@POST
	@Path("/orgs/{id}/backups")
	@Timed
	@UnitOfWork
	public Response createBackup(final @PathParam("id") String id, final @QueryParam("acc") int accId)
			throws SalesforceException {
		final Backup backup = salesforceService.createBackup(id, accId);
		return Response.created(UriBuilder.fromMethod(getClass(), "getBackup").build(id, backup.getDate()))
				.entity(backup).build();
	}

	private String getOrgurl(final Environment request) {
		final String orgURL;
		switch (request) {
		case SANDBOX:
			orgURL = "https://test.salesforce.com";
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
	public Response listBackups(final @PathParam("id") String id) {
		final Set<Backup> backups = salesforceService.listBackups(id);
		backups.size();
		return Response.ok(backups).build();
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

	@DELETE
	@Path("/orgs/{id}/backups/{date}")
	@Timed
	@UnitOfWork
	public Response deleteBackup(final @PathParam("id") String id, final @PathParam("date") String date)
			throws SalesforceException {
		salesforceService.deleteBackup(id, date);
		return Response.noContent().build();
	}

	@POST
	@Path("/orgs/{id}/tests")
	@Timed
	@UnitOfWork
	public Response runTests(final @PathParam("id") String id, final @QueryParam("acc") int accId)
			throws SalesforceException {
		final RunTestsResult runTests = salesforceService.runTests(id, accId, null);
		return Response.created(UriBuilder.fromMethod(getClass(), "showTests").build(id, 1)).entity(runTests).build(); // TODO
																														// get
																														// actual
																														// result
																														// id
	}

	@GET
	@Path("/orgs/{id}/tests/{runId}")
	@Timed
	@UnitOfWork
	public Response showTests(final @PathParam("id") String id, final @PathParam("runId") String runId,
			final @QueryParam("acc") int accId) throws SalesforceException {
		final RunTestsResult runTests = salesforceService.runTests(id, accId, null);
		return Response.ok(runTests).build();
	}
}

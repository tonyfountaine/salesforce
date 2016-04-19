package nz.co.trineo.salesforce;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.AccountService;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.salesforce.model.CodeCoverageResult;
import nz.co.trineo.salesforce.model.TreeNode;
import nz.co.trineo.salesforce.model.Organization;
import nz.co.trineo.salesforce.model.RunTestsResult;
import nz.co.trineo.salesforce.views.CompareView;
import nz.co.trineo.salesforce.views.ContentView;
import nz.co.trineo.salesforce.views.CoverageView;
import nz.co.trineo.salesforce.views.SfOrgView;
import nz.co.trineo.salesforce.views.SfOrgsView;

@Path("/sf")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalesforceResource {

	private final SalesforceService salesforceService;
	private final AccountService accountService;

	@Context
	private Request request;

	public SalesforceResource(final SalesforceService salesforceService, final AccountService accountService) {
		super();
		this.salesforceService = salesforceService;
		this.accountService = accountService;
	}

	@GET
	@Path("/orgs")
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response getOrgs() {
		final List<Organization> orgs = salesforceService.listOrgs();
		final List<ConnectedAccount> accounts = accountService.list();
		final SfOrgsView view = new SfOrgsView(orgs, accounts);
		return Response.ok(view).build();
	}

	@POST
	@Path("/orgs")
	@Timed
	@UnitOfWork
	public Response addOrg(final @QueryParam("acc") int accId) throws SalesforceException {
		final Organization org = salesforceService.addOrg(accId);
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

	@GET
	@Path("/orgs/{id}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response getOrgHtml(final @PathParam("id") String id) throws SalesforceException {
		final Organization org = salesforceService.getOrg(id);
		final Set<String> backups = salesforceService.listBackups(id);
		final List<RunTestsResult> tests = org.getTestResults();
		tests.size();
		final SfOrgView view = new SfOrgView(org, backups, tests);
		return Response.ok(view).build();
	}

	@GET
	@Path("/orgs/{id}/compare/{first}/{second}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response diffBackups(final @PathParam("id") String id, final @PathParam("first") String first,
			final @PathParam("second") String second) throws SalesforceException {
		final List<GitDiff> list = salesforceService.diffBackups(id, first, second);
		final CompareView view = new CompareView(list);
		return Response.ok(view).build();
	}

	@GET
	@Path("/orgs/{idA}/compare/{idB}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response diffOrgs(final @PathParam("idA") String idA, final @PathParam("idB") String idB)
			throws SalesforceException {
		final List<GitDiff> list = salesforceService.diffOrgs(idA, idB);
		final CompareView view = new CompareView(list);
		return Response.ok(view).build();
	}

	@POST
	@Path("/orgs/{id}/metadata")
	@Timed
	@UnitOfWork
	public Response getMetadata(final @PathParam("id") String id, final @QueryParam("acc") int accId)
			throws SalesforceException {
		// salesforceService.downloadAllMetadata(id, accId);
		// TODO add showing the metadata
		return Response.ok().build();
	}

	@GET
	@Path("/orgs/{id}/metadata")
	@Timed
	@UnitOfWork
	public Response getMetadataTree(final @PathParam("id") String id) throws SalesforceException {
		final TreeNode metadataNode = salesforceService.getMetadataTree(id);
		return Response.ok(metadataNode).build();
	}

	@GET
	@Path("/orgs/{id}/metadata/{folder}/{file}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_PLAIN)
	public Response getMetadataContent(final @PathParam("id") String id, final @PathParam("folder") String folder,
			final @PathParam("file") String file) throws SalesforceException {
		final InputStream in = salesforceService.getMetadataContent(id, folder + "/" + file);
		final StreamingOutput stream = output -> {
			try {
				IOUtils.copy(in, output);
			} catch (final Exception e) {
				throw new WebApplicationException(e);
			}
		};

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + folder + "/" + file)
				.build();
	}

	@GET
	@Path("/orgs/{id}/metadata/{folder}/{file}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response getMetadataContentLines(final @PathParam("id") String id, final @PathParam("folder") String folder,
			final @PathParam("file") String file) throws SalesforceException {
		final List<String> lines = salesforceService.getMetadataContentLines(id, folder + "/" + file);
		final CodeCoverageResult result = salesforceService.getCoverageFor(id, file);
		final ContentView view = new ContentView(lines, result);
		return Response.ok(view).build();
	}

	@POST
	@Path("/orgs/{id}/backups")
	@Timed
	@UnitOfWork
	public Response createBackup(final @PathParam("id") String id) throws SalesforceException {
		final String backup = salesforceService.createBackup(id);
		return Response.created(UriBuilder.fromMethod(getClass(), "getBackup").build(id, backup)).entity(backup)
				.build();
	}

	@GET
	@Path("/orgs/{id}/backups")
	@Timed
	@UnitOfWork
	public Response listBackups(final @PathParam("id") String id) throws SalesforceException {
		final Set<String> backups = salesforceService.listBackups(id);
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
		final StreamingOutput stream = output -> {
			try {
				IOUtils.copy(in, output);
			} catch (final Exception e) {
				throw new WebApplicationException(e);
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

	@GET
	@Path("/orgs/{id}/tests")
	@Timed
	@UnitOfWork
	public Response getTestTree(final @PathParam("id") String id) throws SalesforceException {
		final TreeNode metadataNode = salesforceService.getTestTree(id);
		return Response.ok(metadataNode).build();
	}

	@POST
	@Path("/orgs/{id}/tests")
	@Timed
	@UnitOfWork
	public Response runTests(final @PathParam("id") String id) throws SalesforceException {
		final RunTestsResult runTests = salesforceService.runTests(id, null);
		return Response.created(UriBuilder.fromMethod(getClass(), "showTest").build(id, runTests.getId()))
				.entity(runTests).build();
	}

	@GET
	@Path("/orgs/{id}/tests/{runId}")
	@Timed
	@UnitOfWork
	public Response showTest(final @PathParam("id") String id, final @PathParam("runId") String runId)
			throws SalesforceException {
		final RunTestsResult runTests = salesforceService.showTest(runId);

		return Response.ok(runTests).build();
	}

	@GET
	@Path("/orgs/{id}/coverage")
	@Timed
	@UnitOfWork
	@Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
	public Response getCodeCoverage(final @PathParam("id") String id) throws SalesforceException {
		final List<CodeCoverageResult> coverage = salesforceService.getCodeCoverage(id);
		final CoverageView view = new CoverageView(coverage);
		return Response.ok(view).build();
	}
}

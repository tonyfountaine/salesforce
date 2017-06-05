package nz.co.trineo.salesforce;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.model.TreeNode;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.repo.view.BranchView;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.CodeCoverageResult;
import nz.co.trineo.salesforce.model.Environment;
import nz.co.trineo.salesforce.model.Organization;
import nz.co.trineo.salesforce.model.RunTestsResult;
import nz.co.trineo.salesforce.views.BackupView;
import nz.co.trineo.salesforce.views.OrganizationView;

@Path("/sf")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalesforceResource {

	private final SalesforceService salesforceService;

	@Inject
	public SalesforceResource(final SalesforceService salesforceService) {
		this.salesforceService = salesforceService;
	}

	@POST
	@Path("/orgs")
	@Timed
	@UnitOfWork
	public Response addOrg(final @QueryParam("acc") int accId) throws SalesforceException {
		final Organization org = salesforceService.addOrg(accId);
		final OrganizationView view = new OrganizationView(org);
		return Response.created(UriBuilder.fromMethod(getClass(), "getOrg").build(org.getId())).entity(view).build();
	}

	@POST
	@Path("/orgs/{id}/backups")
	@Timed
	@UnitOfWork
	public Response createBackup(final @PathParam("id") String id) throws SalesforceException {
		final Backup backup = salesforceService.createBackup(id);
		final BackupView view = new BackupView(backup);
		return Response.ok(view).build();
	}

	@DELETE
	@Path("/orgs/{id}/backups/{backupId}")
	@Timed
	@UnitOfWork
	public Response deleteBackup(final @PathParam("id") String id, final @PathParam("backupId") int backupId)
			throws SalesforceException {
		salesforceService.deleteBackup(id, backupId);
		return Response.noContent().build();
	}

	@DELETE
	@Path("/orgs/{id}")
	@Timed
	@UnitOfWork
	public Response deleteOrg(final @PathParam("id") String id) throws SalesforceException {
		salesforceService.deleteOrg(id);
		return Response.noContent().build();
	}

	@GET
	@Path("/orgs/{id}/compare/{first}/{second}")
	@Timed
	@UnitOfWork
	public Response diffBackups(final @PathParam("id") String id, final @PathParam("first") int first,
			final @PathParam("second") int second) throws SalesforceException {
		final List<GitDiff> list = salesforceService.diffBackups(id, first, second);
		return Response.ok(list).build();
	}

	@POST
	@Path("/orgs/{id}/compareBranch")
	@Timed
	@UnitOfWork
	public Response diffBranch(final @PathParam("id") String id) throws SalesforceException {
		final List<GitDiff> list = salesforceService.diffBranch(id);
		return Response.ok(list).build();
	}

	@GET
	@Path("/orgs/{idA}/compare/{idB}")
	@Timed
	@UnitOfWork
	public Response diffOrgs(final @PathParam("idA") String idA, final @PathParam("idB") String idB)
			throws SalesforceException {
		final List<GitDiff> list = salesforceService.diffOrgs(idA, idB);
		return Response.ok(list).build();
	}

	@GET
	@Path("/orgs/{id}/backups/{backupId}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadBackup(final @PathParam("id") String id, final @PathParam("backupId") int backupId)
			throws SalesforceException {
		final Backup b = salesforceService.getBackup(backupId);
		final InputStream in = salesforceService.downloadBackup(id, backupId);
		final StreamingOutput stream = output -> {
			try {
				IOUtils.copy(in, output);
			} catch (final Exception e) {
				throw new WebApplicationException(e);
			}
		};

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + b.getName() + ".zip")
				.build();
	}

	@GET
	@Path("/orgs/{id}/backups/{backupId}")
	@Timed
	@UnitOfWork
	public Response getBackup(final @PathParam("id") String id, final @PathParam("backupId") int backupId)
			throws SalesforceException {
		final Backup b = salesforceService.getBackup(backupId);
		final BackupView view = new BackupView(b);
		return Response.ok(view).build();
	}

	@GET
	@Path("/orgs/{id}/coverage")
	@Timed
	@UnitOfWork
	@Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
	public Response getCodeCoverage(final @PathParam("id") String id) throws SalesforceException {
		final List<CodeCoverageResult> coverage = salesforceService.getCodeCoverage(id);
		return Response.ok(coverage).build();
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
	public Response getMetadataContentLines(final @PathParam("id") String id, final @PathParam("folder") String folder,
			final @PathParam("file") String file) throws SalesforceException {
		final List<String> lines = salesforceService.getMetadataContentLines(id, folder + "/" + file);
		return Response.ok(lines).build();
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
	@Path("/orgs/{id}")
	@Timed
	@UnitOfWork
	public Response getOrg(final @PathParam("id") String id) throws SalesforceException {
		final Organization org = salesforceService.getOrg(id);
		final OrganizationView view = new OrganizationView(org);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/orgs/{id}/branch")
	public Response getOrgBranch(final @PathParam("id") String id) throws SalesforceException {
		final Organization org = salesforceService.getOrg(id);
		final BranchView view = new BranchView(org.getBranch());
		return Response.ok(view).build();
	}

	@GET
	@Path("/orgs")
	@Timed
	@UnitOfWork
	public Response getOrgs() {
		final List<Organization> orgs = salesforceService.listOrgs();
		final List<OrganizationView> views = new ArrayList<>();
		orgs.forEach(o -> {
			final OrganizationView v = new OrganizationView(o);
			views.add(v);
		});
		return Response.ok(views).build();
	}

	@GET
	@Path("/orgs/{id}/tests")
	@Timed
	@UnitOfWork
	public Response getTestTree(final @PathParam("id") String id) throws SalesforceException {
		final TreeNode metadataNode = salesforceService.getTestTree(id);
		return Response.ok(metadataNode).build();
	}

	@GET
	@Path("/orgs/{id}/backups")
	@Timed
	@UnitOfWork
	public Response listBackups(final @PathParam("id") String id) throws SalesforceException {
		final List<Backup> backups = salesforceService.listBackups(id);
		final List<BackupView> views = new ArrayList<>();
		backups.forEach(b -> {
			views.add(new BackupView(b));
		});
		return Response.ok(views).build();
	}

	@GET
	@Timed
	@Path("/environments")
	public Response listEnvironments() {
		return Response.ok(EnumSet.allOf(Environment.class)).build();
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

	@PUT
	@Path("/orgs")
	@Timed
	@UnitOfWork
	public Response updateOrg(final Organization org) throws SalesforceException {
		final Organization updatedOrg = salesforceService.updateOrg(org);
		final OrganizationView view = new OrganizationView(updatedOrg);
		return Response.ok(view).build();
	}
}

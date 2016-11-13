package nz.co.trineo.salesforce;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.common.AccountService;
import nz.co.trineo.common.ClientService;
import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.github.model.Branch;
import nz.co.trineo.github.model.Repository;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.CodeCoverageResult;
import nz.co.trineo.salesforce.model.Environment;
import nz.co.trineo.salesforce.model.Organization;
import nz.co.trineo.salesforce.model.RunTestsResult;
import nz.co.trineo.salesforce.model.TreeNode;
import nz.co.trineo.salesforce.views.CompareView;
import nz.co.trineo.salesforce.views.ContentView;
import nz.co.trineo.salesforce.views.CoverageView;
import nz.co.trineo.salesforce.views.SfOrgView;
import nz.co.trineo.salesforce.views.SfOrgsView;

@Path("/sf")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalesforceResource {

	public static class OrganizationView {
		public String id;
		public String name;
		public String organizationType;
		public boolean sandbox;
		public AccountView account;
		public String nickName;
		public BranchView branch;
		public ClientView client;

		public OrganizationView(Organization organization) {
			id = organization.getId();
			name = organization.getName();
			organizationType = organization.getOrganizationType();
			sandbox = organization.isSandbox();
			if (organization.getAccount() != null) {
				account = new AccountView(organization.getAccount());
			}
			nickName = organization.getNickName();
			if (organization.getBranch() != null) {
				branch = new BranchView(organization.getBranch());
			}
			if (organization.getClient() != null) {
				client = new ClientView(organization.getClient());
			}
		}
	}

	public static class BranchView {
		public long id;
		public String sha;
		public String name;
		public String url;
		public RepositoryView repo;

		public BranchView(Branch branch) {
			id = branch.getId();
			sha = branch.getSha();
			name = branch.getName();
			url = branch.getUrl();
			repo = new RepositoryView(branch.getRepo());
		}
	}

	public static class RepositoryView {
		public int id;
		public String name;
		public String cloneURL;

		public RepositoryView(Repository repository) {
			id = repository.getId();
			name = repository.getName();
			cloneURL = repository.getCloneURL();
		}
	}

	public static class ClientView {
		public long id;
		public String name;

		public ClientView(Client client) {
			id = client.getId();
			name = client.getName();
		}
	}

	public static class AccountView {
		public int id;
		public String service;
		public String name;

		public AccountView(ConnectedAccount account) {
			id = account.getId();
			service = account.getService();
			name = account.getName();
		}
	}

	private final SalesforceService salesforceService;
	private final AccountService accountService;
	private final ClientService clientService;

	public SalesforceResource(final SalesforceService salesforceService, final AccountService accountService,
			final ClientService clientService) {
		super();
		this.salesforceService = salesforceService;
		this.accountService = accountService;
		this.clientService = clientService;
	}

	@GET
	@Path("/orgs")
	@Timed
	@UnitOfWork
	public Response getOrgs() {
		final List<Organization> orgs = salesforceService.listOrgs();
		final List<OrganizationView> views = new ArrayList<>();
		orgs.forEach(o -> {
			OrganizationView v = new OrganizationView(o);
			views.add(v);
		});
		return Response.ok(views).build();
	}

	@GET
	@Path("/orgs")
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response getOrgsHTML() {
		final List<Organization> orgs = salesforceService.listOrgs();
		final List<ConnectedAccount> accounts = accountService.byService(salesforceService.getName().toLowerCase());
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

	@PUT
	@Path("/orgs")
	@Timed
	@UnitOfWork
	public Response updateOrg(final Organization org) throws SalesforceException {
		final Organization updatedOrg = salesforceService.updateOrg(org);
		return Response.ok(updatedOrg).build();
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

	@DELETE
	@Path("/orgs/{id}")
	@Timed
	@UnitOfWork
	public Response deleteOrg(final @PathParam("id") String id) throws SalesforceException {
		salesforceService.deleteOrg(id);
		return Response.noContent().build();
	}

	@GET
	@Path("/orgs/{id}")
	@Timed
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	public Response getOrgHtml(final @PathParam("id") String id) throws SalesforceException {
		final Organization org = salesforceService.getOrg(id);
		final List<Backup> backups = org.getBackups();
		final List<RunTestsResult> tests = org.getTestResults();
		tests.size();
		final List<Client> clients = clientService.list();
		final List<Branch> branches = new ArrayList<>();
		if (org.getClient() != null && org.getClient().getRepositories() != null) {
			org.getClient().getRepositories().forEach(r -> {
				r.getBranches().forEach(b -> {
					branches.add(b);
				});
			});
		}
		final SfOrgView view = new SfOrgView(org, backups, tests, clients, branches);
		return Response.ok(view).build();
	}

	@GET
	@Timed
	@UnitOfWork
	@Path("/orgs/{id}/branch")
	public Response getOrgBranch(final @PathParam("id") String id) throws SalesforceException {
		final Organization org = salesforceService.getOrg(id);
		return Response.ok(org.getBranch()).build();
	}

	@GET
	@Path("/orgs/{id}/compare/{first}/{second}")
	@Timed
	@UnitOfWork
	public Response diffBackups(final @PathParam("id") String id, final @PathParam("first") int first,
			final @PathParam("second") int second) throws SalesforceException {
		final List<GitDiff> list = salesforceService.diffBackups(id, first, second);
		final CompareView view = new CompareView(list);
		return Response.ok(view).build();
	}

	@POST
	@Path("/orgs/{id}/compareBranch")
	@Timed
	@UnitOfWork
	public Response diffBranch(final @PathParam("id") String id) throws SalesforceException {
		final List<GitDiff> list = salesforceService.diffBranch(id);
		final CompareView view = new CompareView(list);
		return Response.ok(view).build();
	}

	@GET
	@Path("/orgs/{idA}/compare/{idB}")
	@Timed
	@UnitOfWork
	public Response diffOrgs(final @PathParam("idA") String idA, final @PathParam("idB") String idB)
			throws SalesforceException {
		final List<GitDiff> list = salesforceService.diffOrgs(idA, idB);
		final CompareView view = new CompareView(list);
		return Response.ok(view).build();
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
		final Backup backup = salesforceService.createBackup(id);
		return Response.ok(backup).build();
	}

	@GET
	@Path("/orgs/{id}/backups")
	@Timed
	@UnitOfWork
	public Response listBackups(final @PathParam("id") String id) throws SalesforceException {
		final List<Backup> backups = salesforceService.listBackups(id);
		backups.size();
		return Response.ok(backups).build();
	}

	@GET
	@Path("/orgs/{id}/backups/{backupId}")
	@Timed
	@UnitOfWork
	public Response getBackup(final @PathParam("id") String id, final @PathParam("backupId") int backupId)
			throws SalesforceException {
		final Backup b = salesforceService.getBackup(backupId);
		return Response.ok(b).build();
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

	@DELETE
	@Path("/orgs/{id}/backups/{backupId}")
	@Timed
	@UnitOfWork
	public Response deleteBackup(final @PathParam("id") String id, final @PathParam("backupId") int backupId)
			throws SalesforceException {
		salesforceService.deleteBackup(id, backupId);
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

	@GET
	@Timed
	@Path("/environments")
	public Response listEnvironments() {
		return Response.ok(EnumSet.allOf(Environment.class)).build();
	}
}

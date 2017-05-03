package nz.co.trineo.salesforce;

import static com.sforce.soap.metadata.RetrieveStatus.Failed;
import static com.sforce.soap.metadata.RetrieveStatus.Succeeded;
import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.hibernate.SessionFactory;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.apex.RunTestsRequest;
import com.sforce.soap.apex.SoapConnection;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.Package;
import com.sforce.soap.metadata.PackageTypeMembers;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.fault.ExceptionCode;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SoapFaultException;
import com.uwyn.jhighlight.renderer.XhtmlRenderer;
import com.uwyn.jhighlight.renderer.XmlXhtmlRenderer;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ClientService;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.JobExecutionService;
import nz.co.trineo.common.ServiceRegistry;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.common.model.TreeNode;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.GitServiceException;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.repo.RepoDAO;
import nz.co.trineo.repo.RepoService;
import nz.co.trineo.repo.RepoServiceException;
import nz.co.trineo.repo.model.Branch;
import nz.co.trineo.repo.model.Repository;
import nz.co.trineo.repo.model.RepositoryType;
import nz.co.trineo.salesforce.jobs.BackupCallableJob;
import nz.co.trineo.salesforce.model.*;

/**
 * @author tonyfountaine
 *
 */
public class SalesforceService implements ConnectedService {
	private static final SimpleDateFormat BACKUP_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private static final Log log = LogFactory.getLog(SalesforceService.class);
	private static final Pattern genPattern = Pattern.compile("<!--\\s+.*\\s+-->");

	private final AccountDAO credDAO;
	private final OrganizationDAO orgDAO;
	private final AppConfiguration configuration;
	private final GitService gitService;
	private final ClientService clientService;
	private final TestRunDAO testRunDAO;
	private final BackupDAO backupDAO;
	private final SessionFactory sessionFactory;
	private final String apiVersion;
	private final JobExecutionService executionService;
	private final RepoDAO repoDAO;

	/**
	 * @param dao
	 * @param orgDAO
	 * @param configuration
	 * @param gitService
	 * @param testRunDAO
	 * @throws IOException
	 */
	public SalesforceService(final AccountDAO dao, final OrganizationDAO orgDAO, final AppConfiguration configuration,
			final GitService gitService, final TestRunDAO testRunDAO, final BackupDAO backupDAO,
			final SessionFactory sessionFactory, final ClientService clientService,
			final JobExecutionService executionService, final RepoDAO repoDAO) throws IOException {
		credDAO = dao;
		this.orgDAO = orgDAO;
		this.configuration = configuration;
		this.gitService = gitService;
		this.testRunDAO = testRunDAO;
		this.backupDAO = backupDAO;
		this.sessionFactory = sessionFactory;
		this.clientService = clientService;
		this.executionService = executionService;
		this.repoDAO = repoDAO;
		forceMkdir(configuration.getSalesforceDirectory());
		forceMkdir(configuration.getBackupDirectory());
		apiVersion = configuration.getApiVersion();
	}

	/**
	 * @param accId
	 * @return
	 * @throws SalesforceException
	 */
	public Organization addOrg(final int accId) throws SalesforceException {
		try {
			final ConnectedAccount account = credDAO.get(accId);
			final String organizationId = getOrganizationId(account);

			Organization organization = orgDAO.get(organizationId);
			if (organization == null) {
				organization = getOrganization(account, organizationId);
			}
			organization.setAccount(account);
			final File repoDir = new File(configuration.getSalesforceDirectory(), organizationId);
			if (!gitService.isRepo(repoDir)) {
				gitService.createRepo(repoDir);
			}
			orgDAO.persist(organization);
			return organization;
		} catch (final SalesforceException e) {
			throw e;
		} catch (final GitServiceException e) {
			throw new SalesforceException(e);
		}
	}

	/**
	 * @param object
	 * @return
	 */
	public String authorizeURL(final String baseurl) {
		return baseurl + "/services/oauth2/authorize";
	}

	public Backup checkBackupStatus(final String orgId, final int backupId) throws SalesforceException {
		final Organization org = orgDAO.get(orgId);
		final ConnectedAccount account = org.getAccount();
		final Backup b = backupDAO.get(backupId);
		final RetrieveResult result = checkRetrieveStatus(account, b.getRetrieveId(), false);
		log.info("Retrieve Status: " + result.getStatus());
		if (result.getStatus() == Failed) {
			b.setStatus(BackupStatus.FAILED);
		} else if (result.getStatus() == Succeeded) {
			b.setStatus(BackupStatus.SUCCESSFUL);
		}
		backupDAO.persist(b);
		return b;
	}

	public RetrieveResult checkRetrieveStatus(final ConnectedAccount account, final String asyncResultId,
			final boolean includeZip) throws SalesforceException {
		RetrieveResult result = null;
		MetadataConnection metadataConnection = getMetadataConnection(account);
		try {
			result = metadataConnection.checkRetrieveStatus(asyncResultId, includeZip);
		} catch (final ConnectionException e) {
			if (isInvalidSessionException(e)) {
				refreshToken(account);
				metadataConnection = getMetadataConnection(account);
				try {
					result = metadataConnection.checkRetrieveStatus(asyncResultId, includeZip);
				} catch (final ConnectionException e1) {
					throw new SalesforceException(e1);
				}
			}
		}
		return result;
	}

	/**
	 * @param orgId
	 * @return
	 * @throws SalesforceException
	 */
	public Backup createBackup(final String orgId) throws SalesforceException {
		final Organization org = orgDAO.get(orgId);

		final Backup backup = startBackup(orgId);

		executionService.scheduleJob(new BackupCallableJob(org, backup, configuration.getSalesforceDirectory(),
				sessionFactory, this, gitService));

		return backup;
	}

	/**
	 * @param orgURL
	 * @return
	 * @throws FileNotFoundException
	 */
	private ConnectorConfig createConfig(final String orgURL) throws FileNotFoundException {
		final ConnectorConfig config = new ConnectorConfig();
		final String endpoint = orgURL + "/services/Soap/u/" + apiVersion;
		config.setAuthEndpoint(endpoint);
		config.setServiceEndpoint(endpoint);
		config.setTraceFile("trace.log");
		config.setTraceMessage(true);
		config.setPrettyPrintXml(true);
		config.setManualLogin(false);
		config.setCompression(true);
		return config;
	}

	/**
	 * @param metadata
	 * @return
	 */
	private Package createPackage(final DescribeMetadataObject[] metadata) {
		final List<PackageTypeMembers> types = new ArrayList<>();
		for (final DescribeMetadataObject describeMetadataObject : metadata) {
			final PackageTypeMembers members = new PackageTypeMembers();
			members.setName(describeMetadataObject.getXmlName());
			members.setMembers(new String[] { "*" });

			types.add(members);
		}
		final Package pkg = new Package();
		pkg.setTypes(types.toArray(new PackageTypeMembers[0]));
		pkg.setVersion(apiVersion);
		return pkg;
	}

	/**
	 * @param metadata
	 * @return
	 */
	private RetrieveRequest createRequest(final DescribeMetadataObject[] metadata) {
		final RetrieveRequest request = new RetrieveRequest();
		request.setApiVersion(Double.valueOf(apiVersion));
		request.setUnpackaged(createPackage(metadata));
		return request;
	}

	/**
	 * @param id
	 * @param backupId
	 * @return
	 * @throws SalesforceException
	 */
	public void deleteBackup(final String id, final int backupId) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		final Organization organization = orgDAO.get(id);
		final Backup b = backupDAO.get(backupId);
		organization.getBackups().remove(b);
		try {
			gitService.removeTag(repoDir, b.getName());
			orgDAO.persist(organization);
			backupDAO.delete(backupId);
		} catch (final GitServiceException e) {
			throw new SalesforceException(e);
		}
	}

	public void deleteOrg(final String id) {
		final Organization organization = orgDAO.get(id);
		organization.setClient(null);
		organization.setBranch(null);
		organization.getTestResults().forEach(r -> {
			testRunDAO.delete(r.getId());
		});
		organization.getBackups().forEach(b -> {
			backupDAO.delete(b.getId());
		});
		credDAO.delete(organization.getAccount().getId());
		orgDAO.delete(id);
	}

	/**
	 * @param connection
	 * @return
	 * @throws ConnectionException
	 * @throws SalesforceException
	 */
	private DescribeMetadataObject[] describeMetadata(final ConnectedAccount account) throws SalesforceException {
		MetadataConnection metadataConnection = getMetadataConnection(account);
		DescribeMetadataObject[] metadata = null;
		try {
			final DescribeMetadataResult result = metadataConnection.describeMetadata(Double.valueOf(apiVersion));
			metadata = result.getMetadataObjects();
		} catch (final ConnectionException e) {
			if (isInvalidSessionException(e)) {
				refreshToken(account);
				metadataConnection = getMetadataConnection(account);
				try {
					final DescribeMetadataResult result = metadataConnection
							.describeMetadata(Double.valueOf(apiVersion));
					metadata = result.getMetadataObjects();
				} catch (final ConnectionException e1) {
					throw new SalesforceException(e1);
				}
			}
		}
		return metadata;
	}

	/**
	 * @param id
	 * @param first
	 * @param second
	 * @return
	 * @throws SalesforceException
	 */
	public List<GitDiff> diffBackups(final String id, final int first, final int second) throws SalesforceException {
		final Backup firstBackup = backupDAO.get(first);
		final Backup secondBackup = backupDAO.get(second);
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			return gitService.diff(repoDir, firstBackup.getName(), secondBackup.getName(), null);
		} catch (final GitServiceException e) {
			throw new SalesforceException(e);
		}
	}

	private RepoService getRepoService(final RepositoryType type) {
		return (RepoService) ServiceRegistry.getService(type.name());
	}

	public List<GitDiff> diffBranch(final String id) throws SalesforceException {
		final Organization org = orgDAO.get(id);
		final File repoDirA = new File(configuration.getSalesforceDirectory(), id);
		final Branch branch = org.getBranch();
		final Repository repo = branch.getRepo();
		final File repoDirB = new File(configuration.getGithubDirectory(), repo.getName());
		final RepoService service = getRepoService(repo.getType());

		try {
			if (!service.isRepo(repo.getId())) {
				service.clone(repo.getId());
			}
			service.checkout(repo.getId(), branch.getName());
			service.pull(repo.getId());
			final List<String> paths = pathsFromPackage(repoDirB);
			return gitService.diffReposPath(repoDirA, repoDirB, paths);
		} catch (final RepoServiceException e) {
			throw new SalesforceException(e);
		} finally {
			try {
				gitService.removeRemote(repoDirA);
			} catch (final GitServiceException e) {
				throw new SalesforceException(e);
			}
		}
	}

	/**
	 * @param orgIdA
	 * @param orgIdB
	 * @return
	 * @throws SalesforceException
	 */
	public List<GitDiff> diffOrgs(final String orgIdA, final String orgIdB) throws SalesforceException {
		final File repoDirA = new File(configuration.getSalesforceDirectory(), orgIdA);
		final File repoDirB = new File(configuration.getSalesforceDirectory(), orgIdB);
		try {
			if (!gitService.isRemote(repoDirA, repoDirB)) {
				gitService.addRemote(repoDirA, repoDirB);
			}
			try {
				gitService.fetchRemote(repoDirA);
			} catch (final GitServiceException e) {
				log.error(e);
			}
			return gitService.diffRepos(repoDirA, repoDirB);
		} catch (final GitServiceException e) {
			throw new SalesforceException(e);
		} finally {
			try {
				gitService.removeRemote(repoDirA);
			} catch (final GitServiceException e) {
				throw new SalesforceException(e);
			}
		}
	}

	/**
	 * @param id
	 * @param backupId
	 * @return
	 * @throws SalesforceException
	 */
	public InputStream downloadBackup(final String id, final int backupId) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			final Backup backup = backupDAO.get(backupId);
			gitService.checkout(repoDir, backup.getName());
			final File zipFile = new File(configuration.getBackupDirectory(), backup.getName() + ".zip");
			if (zipFile.exists()) {
				FileUtils.forceDelete(zipFile);
			}
			pack(repoDir, zipFile);
			gitService.checkout(repoDir, "master");
			return new FileInputStream(zipFile);
		} catch (GitServiceException | IOException e) {
			throw new SalesforceException(e);
		}
	}

	/* (non-Javadoc)
	 *
	 * @see nz.co.trineo.common.ConnectedService#getAccessToken(java.lang.String, java.lang.String, java.net.URI,
	 * java.util.Map) */
	@Override
	public AccountToken getAccessToken(final String code, final String state, final URI redirectUri,
			final Map<String, Object> additional) {
		final JerseyClient client = JerseyClientBuilder.createClient();
		final Form entity = new Form();
		entity.param("code", code);
		entity.param("grant_type", "authorization_code");
		entity.param("client_id", getClientId());
		entity.param("client_secret", getClientSecret());
		entity.param("redirect_uri", redirectUri.toString());
		final AccountToken tokenResponse = client
				.target(tokenURL(getOrgurl((Environment) additional.get("environment"))))
				.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(entity, MediaType.APPLICATION_FORM_URLENCODED_TYPE), AccountToken.class);
		return tokenResponse;
	}

	/* (non-Javadoc)
	 *
	 * @see nz.co.trineo.common.ConnectedService#getAuthorizeURIForService(nz.co. trineo.common.model.ConnectedAccount,
	 * java.net.URI, java.lang.String, java.util.Map) */
	@Override
	public URI getAuthorizeURIForService(final ConnectedAccount account, final URI redirectUri, final String state,
			final Map<String, Object> additional) {
		final String uriTemplate = authorizeURL(getOrgurl((Environment) additional.get("environment")))
				+ "?response_type=code&client_id={clientId}&redirect_uri={redirect_uri}&state={state}&scope={scope}&display={display}&prompt={prompt}";
		final URI url = UriBuilder.fromUri(uriTemplate).build(getClientId(), redirectUri, state, "full refresh_token",
				"popup", "login consent");
		return url;
	}

	public Backup getBackup(final int backupId) {
		return backupDAO.get(backupId);
	}

	private BulkConnection getBulkConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final ConnectorConfig config = new ConnectorConfig();
			config.setTraceFile("trace.log");
			config.setTraceMessage(true);
			config.setPrettyPrintXml(true);
			config.setSessionId(account.getToken().getAccessToken());
			config.setRestEndpoint(account.getToken().getInstanceUrl() + "/services/async/" + apiVersion);
			config.setCompression(true);
			return new BulkConnection(config);
		} catch (FileNotFoundException | AsyncApiException e) {
			throw new SalesforceException(e);
		}
	}

	/**
	 * @return
	 */
	public String getClientId() {
		return configuration.getClientKey();
	}

	/**
	 * @return
	 */
	public String getClientSecret() {
		return configuration.getClientSecret();
	}

	/**
	 * @param orgId
	 * @return
	 */
	public List<CodeCoverageResult> getCodeCoverage(final String orgId) {
		final List<RunTestsResult> tests = listTests(orgId);
		if (tests.isEmpty()) {
			return null;
		}
		final RunTestsResult test = tests.get(0);
		final List<CodeCoverageResult> coverage = test.getCodeCoverage();
		for (final CodeCoverageResult codeCoverageResult : coverage) {
			codeCoverageResult.getLocationsNotCovered().size(); // lazy load
		}
		return coverage;
	}

	/**
	 * @param orgId
	 * @param filename
	 * @return
	 */
	public CodeCoverageResult getCoverageFor(final String orgId, final String filename) {
		final List<RunTestsResult> tests = listTests(orgId);
		if (tests.isEmpty()) {
			return null;
		}
		final RunTestsResult test = tests.get(0);
		final String key = filename.split("\\.")[0];
		final Optional<CodeCoverageResult> first = test.getCodeCoverage().stream().filter(ccr -> {
			return key.equals(ccr.getName());
		}).findFirst();
		final CodeCoverageResult result = first.orElse(null);
		return result;
	}

	/**
	 * @param account
	 * @return
	 * @throws SalesforceException
	 */
	private MetadataConnection getMetadataConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final AccountToken token = account.getToken();
			final String instanceUrl = token.getInstanceUrl();
			final ConnectorConfig config = createConfig(instanceUrl);
			config.setSessionId(token.getAccessToken());
			config.setServiceEndpoint(instanceUrl + "/services/Soap/m/" + apiVersion);
			return com.sforce.soap.metadata.Connector.newConnection(config);
		} catch (ConnectionException | FileNotFoundException e) {
			throw new SalesforceException(e);
		}
	}

	/**
	 * @param id
	 * @param path
	 * @return
	 * @throws SalesforceException
	 */
	public InputStream getMetadataContent(final String id, final String path) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		final File file = new File(repoDir, path);
		try {
			return new FileInputStream(file);
		} catch (final FileNotFoundException e) {
			throw new SalesforceException(e);
		}
	}

	/**
	 * @param id
	 * @param path
	 * @return
	 * @throws SalesforceException
	 */
	public List<String> getMetadataContentLines(final String id, final String path) throws SalesforceException {
		try (final InputStream input = getMetadataContent(id, path);) {
			final XhtmlRenderer ren;
			if (path.endsWith(".cls") || path.endsWith(".trigger")) {
				ren = new ApexXhtmlRenderer();
			} else {
				ren = new XmlXhtmlRenderer();
			}
			try (final PipedInputStream in = new PipedInputStream();
					final PipedOutputStream out = new PipedOutputStream(in);) {
				new Thread(() -> {
					try {
						ren.highlight(path, input, out, "UTF-8", true);
					} catch (final Exception e) {
						log.error("Unable to highlight the code", e);
					}
				}).start();
				final List<String> lines = new ArrayList<>();
				try (final BufferedReader b = new BufferedReader(new InputStreamReader(in, "UTF-8"));) {
					b.lines().filter(s -> !genPattern.matcher(s).find()).forEach(i -> {
						lines.add(i.replaceAll("<span class=\"[a-zA-Z0-9_]+\"></span><br />", ""));
					});
					return lines;
				}
			}
		} catch (final IOException e) {
			throw new SalesforceException(e);
		}
	}

	/**
	 * @param orgId
	 * @return
	 * @throws SalesforceException
	 */
	public TreeNode getMetadataTree(final String orgId) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), orgId);
		final Map<String, TreeNode> nodeMap = new HashMap<>();

		final Path path = repoDir.toPath();
		try {
			Files.walk(path).filter(p -> {
				return !p.toString().contains(".git") && !p.toString().endsWith("-meta.xml")
						&& !p.toString().contains("package.xml");
			}).forEach(p -> {
				final TreeNode node = new TreeNode();
				node.setText(p.getFileName().toString());
				nodeMap.put(p.toAbsolutePath().toString(), node);

				final String pp = p.getParent().toAbsolutePath().toString();
				final TreeNode parentNode = nodeMap.get(pp);
				if (parentNode != null) {
					parentNode.getNodes().add(node);
				}
			});
		} catch (final IOException e) {
			throw new SalesforceException(e);
		}

		final TreeNode rootNode = nodeMap.get(path.toAbsolutePath().toString());
		rootNode.setText("/");
		rootNode.getState().setExpanded(true);
		return rootNode;
	}

	/* (non-Javadoc)
	 *
	 * @see nz.co.trineo.common.ConnectedService#getName() */
	@Override
	public String getName() {
		return "Salesforce";
	}

	/**
	 * @param orgId
	 * @return
	 */
	public Organization getOrg(final String orgId) {
		final Organization organization = orgDAO.get(orgId);
		organization.getBackups().size(); // lazy load
		organization.getTestResults().size(); // lazy load
		return organization;
	}

	private Organization getOrganization(final ConnectedAccount account, final String organizationId)
			throws SalesforceException {
		final QueryResult queryResult = runQuery(account,
				"SELECT Id, Name, OrganizationType, IsSandbox FROM Organization WHERE Id = '" + organizationId + "'");
		final SObject sObject = queryResult.getRecords()[0];
		final Organization organization = ConvertUtils.toOrganization(sObject);
		return organization;
	}

	private String getOrganizationId(final ConnectedAccount account) throws SalesforceException {
		PartnerConnection connection = getPartnerConnection(account);
		String organizationId = null;
		try {
			organizationId = connection.getUserInfo().getOrganizationId();
		} catch (final ConnectionException e) {
			if (isInvalidSessionException(e)) {
				refreshToken(account);
				connection = getPartnerConnection(account);
				try {
					organizationId = connection.getUserInfo().getOrganizationId();
				} catch (final ConnectionException e1) {
					throw new SalesforceException(e1);
				}
			}
		}
		return organizationId;
	}

	/**
	 * @param request
	 * @return
	 */
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

	/**
	 * @param account
	 * @return
	 * @throws SalesforceException
	 */
	private PartnerConnection getPartnerConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final AccountToken token = account.getToken();
			final String instanceUrl = token.getInstanceUrl();
			final ConnectorConfig config = createConfig(instanceUrl);
			config.setSessionId(token.getAccessToken());
			config.setServiceEndpoint(instanceUrl + "/services/Soap/u/" + apiVersion);
			return Connector.newConnection(config);
		} catch (ConnectionException | FileNotFoundException e) {
			throw new SalesforceException(e);
		}
	}

	/**
	 * @param account
	 * @return
	 * @throws SalesforceException
	 */
	private SoapConnection getSoapConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final AccountToken token = account.getToken();
			final String instanceUrl = token.getInstanceUrl();
			final ConnectorConfig config = createConfig(instanceUrl);
			config.setSessionId(token.getAccessToken());
			config.setServiceEndpoint(instanceUrl + "/services/Soap/s/" + apiVersion);
			return com.sforce.soap.apex.Connector.newConnection(config);
		} catch (ConnectionException | FileNotFoundException e) {
			throw new SalesforceException(e);
		}
	}

	/**
	 * @param orgId
	 * @return
	 * @throws SalesforceException
	 */
	public TreeNode getTestTree(final String orgId) throws SalesforceException {
		final TreeNode rootNode = new TreeNode();
		rootNode.setText("/");
		rootNode.getState().setExpanded(true);
		rootNode.setIcon("fa fa-cloud");
		final Organization organization = orgDAO.get(orgId);
		final List<RunTestsResult> testResults = organization.getTestResults();
		testResults.forEach(r -> {
			final TreeNode node = new TreeNode();
			node.setText(String.valueOf(r.getId()));
			node.setIcon("fa fa-tasks");
			final List<RunTestMessage> messages = new ArrayList<>();
			final Map<String, TreeNode> nodeMap = new HashMap<>();
			messages.addAll(r.getSuccesses());
			messages.addAll(r.getFailures());
			messages.stream().sorted().forEach(m -> {
				if (!nodeMap.containsKey(m.getName())) {
					final TreeNode classNode = new TreeNode();
					classNode.setText(m.getName());
					nodeMap.put(m.getName(), classNode);
					node.getNodes().add(classNode);
				}
				final TreeNode classNode = nodeMap.get(m.getName());
				final TreeNode messageNode = new TreeNode();
				messageNode.setText(m.getMethodName());
				if (m instanceof RunTestFailure) {
					messageNode.setIcon("fa fa-times");
				} else {
					messageNode.setIcon("fa fa-check");
				}
				classNode.getNodes().add(messageNode);
			});
			rootNode.getNodes().add(node);
		});
		return rootNode;
	}

	/**
	 * Check if the exception was caused by an invalid session
	 *
	 * @param e
	 * @return true if the session was invalid
	 */
	private boolean isInvalidSessionException(final ConnectionException e) {
		log.error("Checking for INVALID_SESSION_ID");
		if (e instanceof SoapFaultException) {
			if (((SoapFaultException) e).getFaultCode().getLocalPart()
					.equals(ExceptionCode.INVALID_SESSION_ID.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param orgId
	 * @return
	 * @throws SalesforceException
	 */
	public List<Backup> listBackups(final String orgId) throws SalesforceException {
		final Organization organization = orgDAO.get(orgId);
		List<Backup> sorted = organization.getBackups();
		sorted.sort((Backup o1, Backup o2) -> {
			try {
				final Date d1 = BACKUP_NAME_FORMAT.parse(o1.getName());
				final Date d2 = BACKUP_NAME_FORMAT.parse(o2.getName());
				return d1.compareTo(d2) * -1;
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return 0;
		});
		return sorted;
	}

	/**
	 * @return
	 */
	public List<Organization> listOrgs() {
		return orgDAO.listAll();
	}

	/**
	 * @param orgId
	 * @return
	 */
	public List<RunTestsResult> listTests(final String orgId) {
		final Organization organization = orgDAO.get(orgId);
		final List<RunTestsResult> testResults = organization.getTestResults();
		testResults.size(); // lazy loading
		return testResults;
	}

	/**
	 * @param folder
	 * @param zipFile
	 * @throws SalesforceException
	 */
	private void pack(final File folder, final File zipFile) throws SalesforceException {
		try {
			final Path p = Files.createFile(zipFile.toPath());
			try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p));) {
				final Path pp = folder.toPath();
				Files.walk(pp)
						.filter(path -> !Files.isDirectory(path) && !path.toAbsolutePath().toString().contains(".git"))
						.forEach(path -> {
							final String sp = path.toAbsolutePath().toString()
									.replace(pp.toAbsolutePath().toString(), "unpackaged")
									.replace(path.getFileName().toString(), "");
							final ZipEntry zipEntry = new ZipEntry(sp + "/" + path.getFileName().toString());
							try {
								zs.putNextEntry(zipEntry);
								zs.write(Files.readAllBytes(path));
								zs.closeEntry();
							} catch (final Exception e) {
								log.error("An error ocurred packing the zip file", e);
							}
						});
			}
		} catch (final IOException e) {
			throw new SalesforceException(e);
		}
	}

	private List<String> pathsFromPackage(final File dir) throws SalesforceException {
		final List<String> paths = new ArrayList<>();
		final Path path = new File(dir, "src").toPath();

		try {
			Files.walk(path).filter(p -> {
				return !p.toString().contains(".git") && Files.isDirectory(p);
			}).forEach(p -> {
				final Path relativize = path.relativize(p);
				final String pp = relativize.toString();
				if (StringUtils.isNotBlank(pp)) {
					paths.add(pp);
				}
			});
		} catch (final IOException e) {
			throw new SalesforceException(e);
		}

		return paths;
	}

	/**
	 * @param account
	 * @param organization
	 * @return
	 * @throws SalesforceException
	 */
	public AccountToken refreshToken(final ConnectedAccount account) throws SalesforceException {
		log.info("refreshing token");
		final JerseyClient client = JerseyClientBuilder.createClient();
		final Form entity = new Form();
		entity.param("grant_type", "refresh_token");
		entity.param("client_id", getClientId());
		entity.param("client_secret", getClientSecret());
		entity.param("refresh_token", account.getToken().getRefreshToken());
		final AccountToken tokenResponse;
		final Organization org = orgDAO.findOrganization(account);
		try {
			final Environment environment = org.isSandbox() ? Environment.SANDBOX : Environment.PRODUCTION;
			tokenResponse = client.target(tokenURL(getOrgurl(environment))).request(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.entity(entity, MediaType.APPLICATION_FORM_URLENCODED_TYPE), AccountToken.class);
		} catch (final Exception e) {
			throw new SalesforceException(e);
		}
		account.getToken().setAccessToken(tokenResponse.getAccessToken());
		credDAO.persist(account);
		return tokenResponse;
	}

	private QueryResult runQuery(final ConnectedAccount account, final String query) throws SalesforceException {
		PartnerConnection connection = getPartnerConnection(account);
		QueryResult queryResult = null;
		try {
			queryResult = connection.query(query);
		} catch (final ConnectionException e) {
			if (isInvalidSessionException(e)) {
				refreshToken(account);
				connection = getPartnerConnection(account);
				try {
					queryResult = connection.query(query);
				} catch (final ConnectionException e1) {
					throw new SalesforceException(e1);
				}
			}
		}
		while (!queryResult.isDone()) {
			log.info(queryResult);
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		return queryResult;
	}

	/**
	 * @param request
	 * @param account
	 * @return
	 * @throws SalesforceException
	 */
	private RunTestsResult runTests(final RunTestsRequest request, final ConnectedAccount account)
			throws SalesforceException {
		SoapConnection soapConnection = getSoapConnection(account);
		com.sforce.soap.apex.RunTestsResult runTestsResult = null;
		try {
			runTestsResult = soapConnection.runTests(request);
		} catch (final ConnectionException e) {
			if (isInvalidSessionException(e)) {
				refreshToken(account);
				soapConnection = getSoapConnection(account);
				try {
					runTestsResult = soapConnection.runTests(request);
				} catch (final ConnectionException e1) {
					throw new SalesforceException(e1);
				}
			}
		}
		if (runTestsResult != null) {
			final RunTestsResult testsResult = ConvertUtils.toRunTestsResult(runTestsResult);
			final RunTestsResult result = testRunDAO.persist(testsResult);
			return result;
		}
		return null;
	}

	/**
	 * @param orgId
	 * @param tests
	 * @return
	 * @throws SalesforceException
	 */
	public RunTestsResult runTests(final String orgId, final String[] tests) throws SalesforceException {
		final Organization organization = orgDAO.get(orgId);
		final ConnectedAccount account = organization.getAccount();
		final RunTestsRequest runTestsRequest = new RunTestsRequest();
		runTestsRequest.setAllTests(tests == null || tests.length == 0);
		runTestsRequest.setClasses(tests);
		runTestsRequest.setNamespace("");
		final RunTestsResult runTests = runTests(runTestsRequest, account);
		organization.getTestResults().add(runTests);
		orgDAO.persist(organization);
		return runTests;
	}

	/**
	 * @param runId
	 * @return
	 */
	public RunTestsResult showTest(final String runId) {
		final RunTestsResult result = testRunDAO.get(runId);
		log.debug(result); // log it to lazy load everything
		return result;
	}

	public Backup startBackup(final String orgId) throws SalesforceException {
		final Organization org = orgDAO.get(orgId);
		final ConnectedAccount account = org.getAccount();

		final DescribeMetadataObject[] metadata = describeMetadata(account);
		final MetadataConnection metadataConnection = getMetadataConnection(account);

		final RetrieveRequest request = createRequest(metadata);
		final AsyncResult asyncResult;
		try {
			asyncResult = metadataConnection.retrieve(request);
		} catch (final ConnectionException e) {
			throw new SalesforceException(e);
		}

		final Backup backup = new Backup();
		final String date = BACKUP_NAME_FORMAT.format(new Date());
		backup.setName(date);
		backup.setRetrieveId(asyncResult.getId());
		backup.setStatus(BackupStatus.IN_PROGRESS);
		org.getBackups().add(backup);
		backupDAO.persist(backup);
		orgDAO.persist(org);
		return backup;
	}

	/**
	 * @param baseurl
	 * @return
	 */
	public String tokenURL(final String baseurl) {
		return baseurl + "/services/oauth2/token";
	}

	public Organization updateOrg(final Organization org) {
		final Organization organization = orgDAO.get(org.getId());
		if (org.getClient() != null) {
			final Client client = clientService.read(org.getClient().getId());
			organization.setClient(client);
			if (!client.getOrganizations().contains(organization)) {
				client.getOrganizations().add(organization);
			}
			clientService.update(client);
		}
		if (org.getBranch() != null) {
			final Branch branch = repoDAO.getBranch(org.getBranch().getId());
			organization.setBranch(branch);
		}
		if (StringUtils.isNotBlank(org.getNickName())) {
			organization.setNickName(org.getNickName());
		}
		orgDAO.persist(organization);
		return organization;
	}

	public Backup updateBackup(final Backup backup) {
		return backupDAO.persist(backup);
	}

	@Override
	public boolean verify(final ConnectedAccount account) {
		try {
			PartnerConnection connection = getPartnerConnection(account);
			try {
				connection.getUserInfo().getOrganizationId();
			} catch (final ConnectionException e) {
				if (isInvalidSessionException(e)) {
					refreshToken(account);
					connection = getPartnerConnection(account);
					try {
						connection.getUserInfo().getOrganizationId();
					} catch (final ConnectionException e1) {
						throw new SalesforceException(e1);
					}
				}
			}
		} catch (final SalesforceException e) {
			log.error("Unable to verify salesforce account", e);
			return false;
		}
		return true;
	}
}
package nz.co.trineo.salesforce;

import static com.sforce.soap.metadata.RetrieveStatus.Failed;
import static com.sforce.soap.metadata.RetrieveStatus.Succeeded;
import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

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
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.GitServiceException;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.salesforce.model.CodeCoverageResult;
import nz.co.trineo.salesforce.model.Environment;
import nz.co.trineo.salesforce.model.Organization;
import nz.co.trineo.salesforce.model.RunTestFailure;
import nz.co.trineo.salesforce.model.RunTestMessage;
import nz.co.trineo.salesforce.model.RunTestsResult;
import nz.co.trineo.salesforce.model.TreeNode;

/**
 * @author tonyfountaine
 *
 */
public class SalesforceService implements ConnectedService {
	private static final Log log = LogFactory.getLog(SalesforceService.class);
	private static final double API_VERSION = 35.0;
	private static final long ONE_SECOND = 1000;
	private static final int MAX_NUM_POLL_REQUESTS = 50;
	private static final Pattern genPattern = Pattern.compile("<!--\\s+.*\\s+-->");

	private final AccountDAO credDAO;
	private final OrganizationDAO orgDAO;
	private final AppConfiguration configuration;
	private final GitService gitService;
	private final TestRunDAO testRunDAO;

	/**
	 * @param dao
	 * @param orgDAO
	 * @param configuration
	 * @param gitService
	 * @param testRunDAO
	 * @throws IOException
	 */
	public SalesforceService(final AccountDAO dao, final OrganizationDAO orgDAO, final AppConfiguration configuration,
			final GitService gitService, final TestRunDAO testRunDAO) throws IOException {
		credDAO = dao;
		this.orgDAO = orgDAO;
		this.configuration = configuration;
		this.gitService = gitService;
		this.testRunDAO = testRunDAO;
		forceMkdir(configuration.getSalesforceDirectory());
		forceMkdir(configuration.getBackupDirectory());
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
	 * @throws SalesforceException
	 */
	public String createBackup(final String orgId) throws SalesforceException {
		try {
			final Organization org = orgDAO.get(orgId);
			final ConnectedAccount account = org.getAccount();

			MetadataConnection metadataConnection = getMetadataConnection(account);
			DescribeMetadataObject[] metadata = null;
			try {
				metadata = describeMetadata(metadataConnection);
			} catch (final ConnectionException e) {
				if (isInvalidSessionException(e)) {
					refreshToken(account);
					metadataConnection = getMetadataConnection(account);
					try {
						metadata = describeMetadata(metadataConnection);
					} catch (final ConnectionException e1) {
						throw new SalesforceException(e1);
					}
				}
			}

			final RetrieveRequest request = createRequest(metadata);
			final AsyncResult asyncResult = metadataConnection.retrieve(request);
			final RetrieveResult result = waitForRetrieveCompletion(metadataConnection, asyncResult);
			if (result.getStatus() == Failed) {
				throw new SalesforceException(
						"code: " + result.getErrorStatusCode() + ", message: " + result.getErrorMessage());
			} else if (result.getStatus() == Succeeded) {
				final DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				final String date = format.format(new Date());
				final String filename = date + ".zip";
				final File idDir = new File(configuration.getBackupDirectory(), orgId);
				final File file = new File(idDir, filename);
				FileUtils.forceMkdir(file.getParentFile());
				final File repoDir = new File(configuration.getSalesforceDirectory(), orgId);
				cleanDirectory(repoDir);
				try (InputStream in = new ByteArrayInputStream(result.getZipFile())) {
					extractMetadataZip(repoDir, in);
				}
				gitService.commit(repoDir, "Backup of all metadata for " + org.getName() + ". timestamp: " + date);
				gitService.tag(repoDir, date);
				return date;
			}
		} catch (final SalesforceException e) {
			throw e;
		} catch (final ConnectionException | InterruptedException | GitServiceException | IOException
				| ArchiveException e) {
			throw new SalesforceException(e);
		}
		return null;
	}

	private void cleanDirectory(final File repoDir) throws IOException {
		final File[] files = repoDir.listFiles((FilenameFilter) (dir, name) -> !name.startsWith("."));
		log.info(files);
		for (final File file : files) {
			FileUtils.forceDelete(file);
		}
	}

	/**
	 * @param id
	 * @param date
	 * @return
	 * @throws SalesforceException
	 */
	public InputStream downloadBackup(final String id, final String date) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			gitService.checkout(repoDir, date);
			final File zipFile = new File(configuration.getBackupDirectory(), date + ".zip");
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
				Files.walk(pp).filter(path -> !Files.isDirectory(path)).forEach(path -> {
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

	/**
	 * @param dir
	 * @param zipIn
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ArchiveException
	 */
	private void extractMetadataZip(final File dir, final InputStream zipIn)
			throws IOException, FileNotFoundException, ArchiveException {
		try (ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP,
				zipIn);) {
			ZipArchiveEntry entry;
			while ((entry = (ZipArchiveEntry) in.getNextEntry()) != null) {
				final File destFile = new File(dir, entry.getName().replaceAll("unpackaged[/\\\\]", ""));
				FileUtils.forceMkdir(destFile.getParentFile());
				try (OutputStream out = new FileOutputStream(destFile);) {
					IOUtils.copy(in, out);
				}
			}
		}
	}

	/**
	 * @param connection
	 * @return
	 * @throws ConnectionException
	 */
	private DescribeMetadataObject[] describeMetadata(final MetadataConnection connection) throws ConnectionException {
		final DescribeMetadataResult result = connection.describeMetadata(API_VERSION);
		return result.getMetadataObjects();
	}

	/**
	 * @param connection
	 * @param asyncResult
	 * @return
	 * @throws InterruptedException
	 * @throws SalesforceException
	 * @throws ConnectionException
	 */
	private RetrieveResult waitForRetrieveCompletion(final MetadataConnection connection, final AsyncResult asyncResult)
			throws SalesforceException, InterruptedException, ConnectionException {
		// Wait for the retrieve to complete
		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND;
		final String asyncResultId = asyncResult.getId();
		RetrieveResult result = null;
		do {
			Thread.sleep(waitTimeMilliSecs);
			// Double the wait time for the next iteration
			waitTimeMilliSecs *= 2;
			if (poll++ > MAX_NUM_POLL_REQUESTS) {
				throw new SalesforceException("Request timed out.  If this is a large set "
						+ "of metadata components, check that the time allowed "
						+ "by MAX_NUM_POLL_REQUESTS is sufficient.");
			}
			result = connection.checkRetrieveStatus(asyncResultId, true);
			log.info("Retrieve Status: " + result.getStatus());
		} while (!result.isDone());

		return result;
	}

	/**
	 * @param metadata
	 * @return
	 */
	private RetrieveRequest createRequest(final DescribeMetadataObject[] metadata) {
		final RetrieveRequest request = new RetrieveRequest();
		request.setApiVersion(API_VERSION);
		request.setUnpackaged(createPackage(metadata));
		return request;
	}

	/**
	 * @param orgURL
	 * @return
	 * @throws FileNotFoundException
	 */
	private ConnectorConfig createConfig(final String orgURL) throws FileNotFoundException {
		final ConnectorConfig config = new ConnectorConfig();
		final String endpoint = orgURL + "/services/Soap/u/35.0";
		config.setAuthEndpoint(endpoint);
		config.setServiceEndpoint(endpoint);
		config.setTraceFile("trace.log");
		config.setTraceMessage(true);
		config.setPrettyPrintXml(true);
		config.setManualLogin(false);
		return config;
	}

	/**
	 * @param account
	 * @return
	 * @throws SalesforceException
	 */
	private PartnerConnection getPartnerConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(account.getToken().getInstanceUrl());
			config.setSessionId(account.getToken().getAccessToken());
			config.setServiceEndpoint(account.getToken().getInstanceUrl() + "/services/Soap/u/35.0");
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
	private MetadataConnection getMetadataConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(account.getToken().getInstanceUrl());
			config.setSessionId(account.getToken().getAccessToken());
			config.setServiceEndpoint(account.getToken().getInstanceUrl() + "/services/Soap/m/35.0");
			return com.sforce.soap.metadata.Connector.newConnection(config);
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
			final ConnectorConfig config = createConfig(account.getToken().getInstanceUrl());
			config.setSessionId(account.getToken().getAccessToken());
			config.setServiceEndpoint(account.getToken().getInstanceUrl() + "/services/Soap/s/35.0");
			return com.sforce.soap.apex.Connector.newConnection(config);
		} catch (ConnectionException | FileNotFoundException e) {
			throw new SalesforceException(e);
		}
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
		pkg.setVersion(String.valueOf(API_VERSION));
		return pkg;
	}

	/**
	 * @param id
	 * @return
	 * @throws SalesforceException
	 */
	public Set<String> listBackups(final String id) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			return gitService.getTags(repoDir);
		} catch (final GitServiceException e) {
			throw new SalesforceException(e);
		}
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
		final RunTestsResult testsResult = ConvertUtils.toRunTestsResult(runTestsResult);
		final RunTestsResult result = testRunDAO.persist(testsResult);
		return result;
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
	 * @param runId
	 * @return
	 */
	public RunTestsResult showTest(final String runId) {
		final RunTestsResult result = testRunDAO.get(runId);
		log.debug(result); // log it to lazy load everything
		return result;
	}

	/**
	 * @return
	 */
	public List<Organization> listOrgs() {
		return orgDAO.listAll();
	}

	/**
	 * @param accId
	 * @return
	 * @throws SalesforceException
	 */
	public Organization addOrg(final int accId) throws SalesforceException {
		try {
			final ConnectedAccount account = credDAO.get(accId);
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

			Organization organization = orgDAO.get(organizationId);
			if (organization == null) {
				final QueryResult queryResult = connection
						.query("SELECT Id, Name, OrganizationType, IsSandbox FROM Organization WHERE Id = '"
								+ organizationId + "'");
				while (!queryResult.isDone()) {
					log.info(queryResult);
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
				final SObject sObject = queryResult.getRecords()[0];
				organization = ConvertUtils.toOrganization(sObject);
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
		} catch (ConnectionException | GitServiceException e) {
			throw new SalesforceException(e);
		}
	}

	/**
	 * @param orgId
	 * @return
	 */
	public Organization getOrg(final String orgId) {
		return orgDAO.get(orgId);
	}

	/**
	 * @param id
	 * @param date
	 * @return
	 * @throws SalesforceException
	 */
	public List<String> deleteBackup(final String id, final String date) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			return gitService.removeTag(repoDir, date);
		} catch (final GitServiceException e) {
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
			codeCoverageResult.getLocationsNotCovered().size();
		}
		return coverage;
	}

	/**
	 * @param id
	 * @param first
	 * @param second
	 * @return
	 * @throws SalesforceException
	 */
	public List<GitDiff> diffBackups(final String id, final String first, final String second)
			throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			return gitService.diff(repoDir, first, second);
		} catch (final GitServiceException e) {
			throw new SalesforceException(e);
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
	 * @param baseurl
	 * @return
	 */
	public String tokenURL(final String baseurl) {
		return baseurl + "/services/oauth2/token";
	}

	/**
	 * @param object
	 * @return
	 */
	public String authorizeURL(final String baseurl) {
		return baseurl + "/services/oauth2/authorize";
	}

	/* (non-Javadoc)
	 *
	 * @see nz.co.trineo.common.ConnectedService#getAuthorizeURIForService(nz.co.trineo.common.model.ConnectedAccount,
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

	/* @Override public URI verify(final ConnectedAccount account) { try { PartnerConnection connection =
	 * getPartnerConnection(account); String organizationId; organizationId =
	 * connection.getUserInfo().getOrganizationId(); } catch (final ConnectionException | SalesforceException e) {
	 *
	 * } return null; } */

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
}
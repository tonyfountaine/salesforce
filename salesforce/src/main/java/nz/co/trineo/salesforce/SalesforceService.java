package nz.co.trineo.salesforce;

import static com.sforce.soap.metadata.RetrieveStatus.Failed;
import static com.sforce.soap.metadata.RetrieveStatus.Succeeded;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
import com.sforce.soap.apex.RunTestsResult;
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
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.GitServiceException;
import nz.co.trineo.salesforce.model.Organization;

public class SalesforceService implements ConnectedService {
	private static final Log log = LogFactory.getLog(SalesforceService.class);
	private static final double API_VERSION = 35.0;
	private static final long ONE_SECOND = 1000;
	private static final int MAX_NUM_POLL_REQUESTS = 50;
	private final AccountDAO credDAO;
	private final OrganizationDAO orgDAO;
	private final AppConfiguration configuration;
	private final GitService gitService;

	public SalesforceService(final AccountDAO dao, final OrganizationDAO orgDAO, final AppConfiguration configuration,
			final GitService gitService) throws IOException {
		this.credDAO = dao;
		this.orgDAO = orgDAO;
		this.configuration = configuration;
		this.gitService = gitService;
		forceMkdir(configuration.getSalesforceDirectory());
		forceMkdir(configuration.getBackupDirectory());
	}

	@Override
	public String getName() {
		return "Salesforce";
	}

	public String createBackup(final String orgId, final int accId) throws SalesforceException {
		try {
			final ConnectedAccount account = credDAO.get(accId);
			final Organization org = orgDAO.get(orgId);
			final MetadataConnection metadataConnection = getMetadataConnection(account);

			final DescribeMetadataObject[] metadata = describeMetadata(metadataConnection);

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
				try (InputStream in = new ByteArrayInputStream(result.getZipFile())) {
					extractMetadataZip(repoDir, in);
				}
				gitService.commit(repoDir, "Backup of all metadata for " + org.getName() + ". timestamp: " + date);
				gitService.tag(repoDir, date);
				return date;
			}
		} catch (SalesforceException e) {
			throw e;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
		return null;
	}

	public InputStream downloadBackup(final String id, final String date) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			gitService.checkout(repoDir, date);
			final File zipFile = new File(configuration.getBackupDirectory(), date + ".zip");
			pack(repoDir, zipFile);
			gitService.checkout(repoDir, "master");
			return new FileInputStream(zipFile);
		} catch (GitServiceException | IOException e) {
			throw new SalesforceException(e);
		}
	}

	private void pack(final File folder, final File zipFile) throws SalesforceException {
		try {
			final Path p = Files.createFile(zipFile.toPath());
			try(ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p));) {
				final Path pp = folder.toPath();
				Files.walk(pp).filter(path -> !Files.isDirectory(path)).forEach(path -> {
					final String sp = path.toAbsolutePath().toString().replace(pp.toAbsolutePath().toString(), "unpackaged")
							.replace(path.getFileName().toString(), "");
					final ZipEntry zipEntry = new ZipEntry(sp + "/" + path.getFileName().toString());
					try {
						zs.putNextEntry(zipEntry);
						zs.write(Files.readAllBytes(path));
						zs.closeEntry();
					} catch (Exception e) {
						log.error("An error ocurred packing the zip file", e);
					}
				});
			}
		} catch (IOException e) {
			throw new SalesforceException(e);
		}
	}

	private void extractMetadataZip(final File dir, final InputStream zipIn)
			throws IOException, FileNotFoundException, ArchiveException {
		try (ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP,
				zipIn);) {
			ZipArchiveEntry entry;
			while ((entry = (ZipArchiveEntry) in.getNextEntry()) != null) {
				final File destFile = new File(dir, entry.getName().replaceAll("unpackaged[/\\]", ""));
				FileUtils.forceMkdir(destFile.getParentFile());
				try (OutputStream out = new FileOutputStream(destFile);) {
					IOUtils.copy(in, out);
				}
			}
		}
	}

	private DescribeMetadataObject[] describeMetadata(final MetadataConnection connection) throws SalesforceException {
		try {
			final DescribeMetadataResult result = connection.describeMetadata(API_VERSION);
			return result.getMetadataObjects();

		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private RetrieveResult waitForRetrieveCompletion(final MetadataConnection connection, AsyncResult asyncResult)
			throws Exception {
		// Wait for the retrieve to complete
		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND;
		String asyncResultId = asyncResult.getId();
		RetrieveResult result = null;
		do {
			Thread.sleep(waitTimeMilliSecs);
			// Double the wait time for the next iteration
			waitTimeMilliSecs *= 2;
			if (poll++ > MAX_NUM_POLL_REQUESTS) {
				throw new Exception("Request timed out.  If this is a large set "
						+ "of metadata components, check that the time allowed "
						+ "by MAX_NUM_POLL_REQUESTS is sufficient.");
			}
			result = connection.checkRetrieveStatus(asyncResultId, true);
			log.info("Retrieve Status: " + result.getStatus());
		} while (!result.isDone());

		return result;
	}

	private RetrieveRequest createRequest(final DescribeMetadataObject[] metadata) {
		final RetrieveRequest request = new RetrieveRequest();
		request.setApiVersion(API_VERSION);
		request.setUnpackaged(createPackage(metadata));
		return request;
	}

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

	private PartnerConnection getPartnerConnection(final ConnectorConfig config) throws SalesforceException {
		try {
			final PartnerConnection connection = Connector.newConnection(config);
			return connection;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private PartnerConnection getPartnerConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(account.getToken().getInstanceUrl());
			if (account.getToken() != null && isNotBlank(account.getToken().getAccessToken())) {
				config.setSessionId(account.getToken().getAccessToken());
				config.setServiceEndpoint(account.getToken().getInstanceUrl() + "/services/Soap/u/35.0");
			}
			final PartnerConnection connection = Connector.newConnection(config);
			return connection;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private MetadataConnection getMetadataConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(account.getToken().getInstanceUrl());
			if (account.getToken() != null && isNotBlank(account.getToken().getAccessToken())) {
				config.setSessionId(account.getToken().getAccessToken());
				config.setServiceEndpoint(account.getToken().getInstanceUrl() + "/services/Soap/m/35.0");
			}
			return com.sforce.soap.metadata.Connector.newConnection(config);
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private SoapConnection getSoapConnection(final ConnectedAccount account) throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(account.getToken().getInstanceUrl());
			if (account.getToken() != null && isNotBlank(account.getToken().getAccessToken())) {
				config.setSessionId(account.getToken().getAccessToken());
				config.setServiceEndpoint(account.getToken().getInstanceUrl() + "/services/Soap/s/35.0");
			}
			return com.sforce.soap.apex.Connector.newConnection(config);
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

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

	public Set<String> listBackups(final String id) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			return gitService.getTags(repoDir);
		} catch (GitServiceException e) {
			throw new SalesforceException(e);
		}
	}

	public RunTestsResult runTests(final int accId, final String[] tests) throws SalesforceException {
		try {
			final ConnectedAccount account = credDAO.get(accId);
			final SoapConnection soapConnection = getSoapConnection(account);
			final RunTestsRequest runTestsRequest = new RunTestsRequest();
			runTestsRequest.setAllTests(tests == null || tests.length == 0);
			runTestsRequest.setClasses(tests);
			final RunTestsResult runTestsResult = soapConnection.runTests(runTestsRequest);
			return runTestsResult;
		} catch (ConnectionException e) {
			throw new SalesforceException(e);
		}
	}

	public Organization addOrg(final int accId) throws SalesforceException {
		try {
			final ConnectedAccount account = credDAO.get(accId);
			final PartnerConnection connection = getPartnerConnection(account);

			final String organizationId = connection.getUserInfo().getOrganizationId();
			final QueryResult queryResult = connection
					.query("SELECT Id, Name, OrganizationType, IsSandbox FROM Organization WHERE Id = '"
							+ organizationId + "'");
			while (!queryResult.isDone()) {
				log.info(queryResult);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			final SObject sObject = queryResult.getRecords()[0];
			final Organization organization = toOrganization(sObject);
			final File repoDir = new File(configuration.getSalesforceDirectory(), organizationId);
			gitService.createRepo(repoDir);
			orgDAO.persist(organization);
			return organization;
		} catch (SalesforceException e) {
			throw e;
		} catch (ConnectionException | GitServiceException e) {
			throw new SalesforceException(e);
		}
	}

	public Organization getOrg(final String orgId) {
		return orgDAO.get(orgId);
	}

	private Organization toOrganization(final SObject sObject) {
		log.info(sObject);
		final Organization organization = new Organization();
		organization.setId(sObject.getId());
		organization.setName((String) sObject.getField("Name"));
		organization.setOrganizationType((String) sObject.getField("OrganizationType"));
		organization.setSandbox(Boolean.valueOf((String) sObject.getField("IsSandbox")));
		return organization;
	}

	public List<String> deleteBackup(String id, String date) throws SalesforceException {
		final File repoDir = new File(configuration.getSalesforceDirectory(), id);
		try {
			return gitService.removeTag(repoDir, date);
		} catch (GitServiceException e) {
			throw new SalesforceException(e);
		}
	}

	public String getClientId() {
		return configuration.getClientKey();
	}

	public String getClientSecret() {
		return configuration.getClientSecret();
	}

	public String tokenURL() {
		return "https://login.salesforce.com/services/oauth2/token";
	}

	public String authorizeURL() {
		return "https://login.salesforce.com/services/oauth2/authorize";
	}

	@Override
	public URI getAuthorizeURIForService(final ConnectedAccount account, final URI redirectUri, final String state) {
		final String uriTemplate = authorizeURL()
				+ "?response_type=code&client_id={clientId}&redirect_uri={redirect_uri}&state={state}&scope={scope}&display={display}&prompt={prompt}";
		final URI url = UriBuilder.fromUri(uriTemplate).build(getClientId(), redirectUri, state, "full refresh_token",
				"popup", "login consent");
		return url;
	}

	@Override
	public AccountToken getAccessToken(final String code, final String state, final URI redirectUri) {
		final JerseyClient client = JerseyClientBuilder.createClient();
		final Form entity = new Form();
		entity.param("code", code);
		entity.param("grant_type", "authorization_code");
		entity.param("client_id", getClientId());
		entity.param("client_secret", getClientSecret());
		entity.param("redirect_uri", redirectUri.toString());
		final AccountToken tokenResponse = client.target(tokenURL()).request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(entity, MediaType.APPLICATION_FORM_URLENCODED_TYPE), AccountToken.class);
		return tokenResponse;
	}
}
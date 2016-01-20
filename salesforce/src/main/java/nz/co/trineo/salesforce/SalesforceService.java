package nz.co.trineo.salesforce;

import static com.sforce.soap.metadata.RetrieveStatus.Failed;
import static com.sforce.soap.metadata.RetrieveStatus.Succeeded;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.FilenameUtils.getBaseName;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.Organization;

public class SalesforceService implements ConnectedService {
	private static final Log log = LogFactory.getLog(SalesforceService.class);
	private static final double API_VERSION = 35.0;
	private static final long ONE_SECOND = 1000;
	private static final int MAX_NUM_POLL_REQUESTS = 50;
	private final AccountDAO credDAO;
	private final OrganizationDAO orgDAO;
	private final BackupDAO backupDAO;
	private final AppConfiguration configuration;

	public SalesforceService(final AccountDAO dao, final OrganizationDAO orgDAO, final BackupDAO backupDAO,
			final AppConfiguration configuration) throws IOException {
		this.credDAO = dao;
		this.orgDAO = orgDAO;
		this.backupDAO = backupDAO;
		this.configuration = configuration;
		forceMkdir(configuration.getSalesforceDirectory());
		forceMkdir(configuration.getBackupDirectory());
	}
	
	@Override
	public String getName() {
		return "Salesforce";
	}

	@SuppressWarnings("unchecked")
	public void updateBackupsFromFilesystem() {
		final File dir = configuration.getBackupDirectory();
		final Iterator<File> files = FileUtils.iterateFiles(dir, new String[] { "zip" }, true);
		for (; files.hasNext();) {
			final File file = files.next();
			final String date = getBaseName(file.getName());
			final String ordId = getBaseName(file.getParent());
			final Organization organization = getOrg(ordId);
			if (organization == null) {
				continue;
			}
			if (backupDAO.get(organization, date) == null) {
				final Backup backup = new Backup();
				backup.setDate(date);
				backup.setOrganization(organization);
				organization.getBackups().add(backup);
				backupDAO.persist(backup);
				orgDAO.persist(organization);
			}
		}
	}

	public void downloadAllMetadata(final String orgId, final int accId) throws SalesforceException {
		final Backup backup = createBackup(orgId, accId);
		try {
			final InputStream in = downloadBackup(orgId, backup.getDate());
			extractMetadataZip(orgId, in);
		} catch (IOException | ArchiveException e) {
			throw new SalesforceException(e);
		}
	}

	public Backup createBackup(final String orgId, final int accId) throws SalesforceException {
		try {
			final Organization org = getOrg(orgId);
			final MetadataConnection metadataConnection = getMetadataConnection(org, accId);

			final DescribeMetadataObject[] metadata = describeMetadata(metadataConnection);

			final RetrieveRequest request = createRequest(metadata);
			final AsyncResult asyncResult = metadataConnection.retrieve(request);
			final RetrieveResult result = waitForRetrieveCompletion(metadataConnection, asyncResult);
			if (result.getStatus() == Failed) {
				throw new SalesforceException(
						"code: " + result.getErrorStatusCode() + ", message: " + result.getErrorMessage());
			} else if (result.getStatus() == Succeeded) {
				final DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				final Backup backup = new Backup();
				backup.setDate(format.format(new Date()));
				backup.setOrganization(org);
				org.getBackups().add(backup);
				final String filename = backup.getDate() + ".zip";
				final File idDir = new File(configuration.getBackupDirectory(), org.getId());
				final File file = new File(idDir, filename);
				FileUtils.forceMkdir(file.getParentFile());
				try (InputStream in = new ByteArrayInputStream(result.getZipFile());
						OutputStream out = new FileOutputStream(file)) {
					IOUtils.copy(in, out);
				}
				backupDAO.persist(backup);
				orgDAO.persist(org);
				return backup;
			}
		} catch (SalesforceException e) {
			throw e;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
		return null;
	}

	// private AccessToken getAccessToken(final String orgURL) {
	// final Credentals credentals = currentCredentals();
	// final Client newClient = JerseyClientBuilder.newClient();
	// final AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
	// accessTokenRequest.client_id = configuration.getClientKey();
	// accessTokenRequest.client_secret = configuration.getClientSecret();
	// accessTokenRequest.grant_type = "password";
	// accessTokenRequest.password = credentals.getPassword();
	// accessTokenRequest.username = credentals.getUsername();
	// return newClient.target(orgURL).path("services/oauth2/token").request()
	// .buildPost(Entity.entity(accessTokenRequest,
	// MediaType.APPLICATION_FORM_URLENCODED_TYPE)).invoke()
	// .readEntity(AccessToken.class);
	// }
	//
	// private Builder getBuilder(final String fullURL, final String
	// accessToken) {
	// final Client newClient = JerseyClientBuilder.newClient();
	// return newClient.target(fullURL).request().header("Authorization",
	// "Bearer " + accessToken);
	// }
	//
	// private Builder getBuilder(final String instanceURL, final String
	// accessToken, final String path) {
	// final Client newClient = JerseyClientBuilder.newClient();
	// return
	// newClient.target(instanceURL).path("services/data/v35.0").path(path).request().header("Authorization",
	// "Bearer " + accessToken);
	// }
	//
	// private UserInfo getUserInfo(final String orgURL) {
	// final AccessToken accessToken = getAccessToken(orgURL);
	// return getUserInfo(accessToken.id, accessToken.access_token);
	// }
	//
	// private UserInfo getUserInfo(final String idURL, final String
	// accessToken) {
	// final Builder builder = getBuilder(idURL, accessToken);
	// return builder.buildGet().invoke(UserInfo.class);
	// }
	//
	// private Organization getOrg(final String orgURL) {
	// final AccessToken accessToken = getAccessToken(orgURL);
	// final UserInfo userInfo = getUserInfo(accessToken.id,
	// accessToken.access_token);
	// return getOrg(accessToken.instance_url, accessToken.access_token,
	// userInfo.organization_id);
	// }
	//
	// private Organization getOrg(final String instanceURL, final String
	// accessToken, final String id) {
	// final Builder builder = getBuilder(instanceURL, accessToken,
	// "sobjects/Organization/" + id + "?fields=Name");
	// return builder.buildGet().invoke(Organization.class);
	// }

	public InputStream downloadBackup(final String id, final String date) throws SalesforceException {
		final Organization organization = getOrg(id);
		final Backup backup = backupDAO.get(organization, date);
		final String filename = backup.getDate() + ".zip";
		try {
			final File idDir = new File(configuration.getBackupDirectory(), organization.getId());
			final File file = new File(idDir, filename);
			log.info(file);
			log.info(file.exists());
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new SalesforceException(e);
		}
	}

	private void extractMetadataZip(final String orgId, final InputStream zipIn)
			throws IOException, FileNotFoundException, ArchiveException {
		try (ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP,
				zipIn);) {
			ZipArchiveEntry entry;
			while ((entry = (ZipArchiveEntry) in.getNextEntry()) != null) {
				final File idDir = new File(configuration.getSalesforceDirectory(), orgId);
				final File destFile = new File(idDir, entry.getName());
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
		config.setManualLogin(true);
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

	private PartnerConnection getPartnerConnection(final String orgURL, final ConnectedAccount account)
			throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(orgURL);
			config.setManualLogin(false);
			config.setUsername(account.getCredentals().getUsername());
			config.setPassword(account.getCredentals().getPassword());
			final PartnerConnection connection = Connector.newConnection(config);
			return connection;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private LoginResult login(final PartnerConnection connection, final int accId) throws SalesforceException {
		try {
			final ConnectedAccount account = credDAO.get(accId);
			final LoginResult result = connection.login(account.getCredentals().getUsername(),
					account.getCredentals().getPassword());
			return result;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private MetadataConnection getMetadataConnection(final Organization organization, final int accId)
			throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(organization.getAuthUrl());
			final PartnerConnection connection = getPartnerConnection(config);
			final LoginResult result = login(connection, accId);

			config.setServiceEndpoint(result.getMetadataServerUrl());
			config.setSessionId(result.getSessionId());
			return com.sforce.soap.metadata.Connector.newConnection(config);
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private SoapConnection getSoapConnection(final Organization organization, final int accId)
			throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(organization.getAuthUrl());
			final PartnerConnection connection = getPartnerConnection(config);
			final LoginResult result = login(connection, accId);

			config.setServiceEndpoint(result.getServerUrl().replace("/u/", "/s/"));
			config.setSessionId(result.getSessionId());
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

	public Set<Backup> listBackups(final String id) {
		return getOrg(id).getBackups();
	}

	public RunTestsResult runTests(final String orgId, final int accId, final String[] tests)
			throws SalesforceException {
		try {
			final Organization organization = getOrg(orgId);
			final SoapConnection soapConnection = getSoapConnection(organization, accId);
			final RunTestsRequest runTestsRequest = new RunTestsRequest();
			runTestsRequest.setAllTests(tests == null || tests.length == 0);
			runTestsRequest.setClasses(tests);
			final RunTestsResult runTestsResult = soapConnection.runTests(runTestsRequest);
			return runTestsResult;
		} catch (ConnectionException e) {
			throw new SalesforceException(e);
		}
	}

	public Organization addOrg(final String endpoint, final int accId) throws SalesforceException {
		try {
			final ConnectedAccount account = credDAO.get(accId);
			final PartnerConnection connection = getPartnerConnection(endpoint, account);

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
			organization.setAuthUrl(endpoint);
			orgDAO.persist(organization);
			return organization;
		} catch (SalesforceException e) {
			throw e;
		} catch (ConnectionException e) {
			throw new SalesforceException(e);
		}
	}

	public Organization getOrg(final String orgId) {
		return orgDAO.get(orgId);
	}

	private Organization toOrganization(SObject sObject) {
		log.info(sObject);
		final Organization organization = new Organization();
		organization.setId(sObject.getId());
		organization.setName((String) sObject.getField("Name"));
		organization.setOrganizationType((String) sObject.getField("OrganizationType"));
		organization.setSandbox(Boolean.valueOf((String) sObject.getField("IsSandbox")));
		return organization;
	}

	public Backup deleteBackup(String id, String date) throws SalesforceException {
		final Organization organization = getOrg(id);
		final Backup backup = backupDAO.get(organization, date);
		final String filename = backup.getDate() + ".zip";
		try {
			final File idDir = new File(configuration.getBackupDirectory(), organization.getId());
			final File file = new File(idDir, filename);
			FileUtils.forceDelete(file);
			organization.getBackups().remove(backup);
			backupDAO.delete(backup);
			return backup;
		} catch (IOException e) {
			throw new SalesforceException(e);
		}
	}
}
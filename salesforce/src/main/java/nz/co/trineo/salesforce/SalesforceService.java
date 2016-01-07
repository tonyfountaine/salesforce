package nz.co.trineo.salesforce;

import static com.sforce.soap.metadata.RetrieveStatus.Failed;
import static com.sforce.soap.metadata.RetrieveStatus.Succeeded;

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
import java.util.List;

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

import nz.co.trineo.common.CredentalsDAO;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.Organization;

public class SalesforceService {
	private static final Log log = LogFactory.getLog(SalesforceService.class);
	private static final double API_VERSION = 35.0;
	private static final long ONE_SECOND = 1000;
	private static final int MAX_NUM_POLL_REQUESTS = 50;
	private final CredentalsDAO credDAO;
	private final OrganizationDAO orgDAO;
	private final BackupDAO backupDAO;
	private final AppConfiguration configuration;

	public SalesforceService(final CredentalsDAO dao, final OrganizationDAO orgDAO, final BackupDAO backupDAO,
			final AppConfiguration configuration) throws IOException {
		this.credDAO = dao;
		this.orgDAO = orgDAO;
		this.backupDAO = backupDAO;
		this.configuration = configuration;
		FileUtils.forceMkdir(configuration.getSalesforceDirectory());
	}

	public Credentals currentCredentals(final String orgId) {
		return credDAO.get(orgId);
	}

	public Credentals updateCredentals(final String orgId, final Credentals credentals) {
		credentals.setId(orgId);
		credDAO.persist(credentals);
		return currentCredentals(orgId);
	}

	public void downloadAllMetadata(final String orgId, final String orgURL) throws SalesforceException {
		final Backup backup = createBackup(orgId, orgURL);
		try {
			final InputStream in = downloadBackup(orgId, backup.getDate());
			extractMetadataZip(orgId, in);
		} catch (IOException | ArchiveException e) {
			throw new SalesforceException(e);
		}
	}

	public Backup createBackup(final String orgId, final String orgURL) throws SalesforceException {
		try {
			final MetadataConnection metadataConnection = getMetadataConnection(orgId, orgURL);

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
				backup.setOrganizationId(orgId);
				final String filename = backup.getDate() + ".zip";
				final File idDir = new File(configuration.getBackupDirectory(), orgId);
				final File file = new File(idDir, filename);
				FileUtils.forceMkdir(file.getParentFile());
				try (InputStream in = new ByteArrayInputStream(result.getZipFile());
						OutputStream out = new FileOutputStream(file)) {
					IOUtils.copy(in, out);
				}
				backupDAO.persist(backup);
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
		final Backup backup = backupDAO.get(id, date);
		final String filename = backup.getDate() + ".zip";
		try {
			final File idDir = new File(configuration.getBackupDirectory(), id);
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
			System.out.println("Retrieve Status: " + result.getStatus());
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

	private PartnerConnection getPartnerConnection(final String orgURL, final Credentals credentals)
			throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(orgURL);
			config.setManualLogin(false);
			config.setUsername(credentals.getUsername());
			config.setPassword(credentals.getPassword());
			final PartnerConnection connection = Connector.newConnection(config);
			return connection;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private LoginResult login(final String orgId, final PartnerConnection connection) throws SalesforceException {
		try {
			final Credentals credentals = currentCredentals(orgId);
			final LoginResult result = connection.login(credentals.getUsername(), credentals.getPassword());
			return result;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private MetadataConnection getMetadataConnection(final String orgId, final String orgURL)
			throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(orgURL);
			final PartnerConnection connection = getPartnerConnection(config);
			final LoginResult result = login(orgId, connection);

			config.setServiceEndpoint(result.getMetadataServerUrl());
			config.setSessionId(result.getSessionId());
			return com.sforce.soap.metadata.Connector.newConnection(config);
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private SoapConnection getSoapConnection(final String orgId, final String orgURL) throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(orgURL);
			final PartnerConnection connection = getPartnerConnection(config);
			final LoginResult result = login(orgId, connection);

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

	// @SuppressWarnings("unchecked")
	public List<Backup> listBackups(final String id) {
		return backupDAO.listBackupsByOrg(id);
		// final List<Backup> backups = new ArrayList<>();
		// final File idDir = new File(configuration.getBackupDirectory(), id);
		// final Iterator<File> files = iterateFiles(idDir, new String[] { "zip"
		// }, false);
		// for (; files.hasNext();) {
		// final File file = files.next();
		// final Backup backup = new Backup();
		// final String baseName = getBaseName(file.getName());
		// backup.setDate(baseName);
		// backups.add(backup);
		// }
		// return backups;
	}

	public RunTestsResult runTests(final String orgId, final String orgURL, final String[] tests)
			throws SalesforceException {
		try {
			final SoapConnection soapConnection = getSoapConnection(orgId, orgURL);
			final RunTestsRequest runTestsRequest = new RunTestsRequest();
			runTestsRequest.setAllTests(tests == null || tests.length == 0);
			runTestsRequest.setClasses(tests);
			final RunTestsResult runTestsResult = soapConnection.runTests(runTestsRequest);
			return runTestsResult;
		} catch (ConnectionException e) {
			throw new SalesforceException(e);
		}
	}

	public Organization getOrg(final String endpoint, final Credentals credentals) throws SalesforceException {
		try {
			final PartnerConnection connection = getPartnerConnection(endpoint, credentals);

			final String organizationId = connection.getUserInfo().getOrganizationId();
			final QueryResult queryResult = connection
					.query("SELECT Id, Name FROM Organization WHERE Id = '" + organizationId + "'");
			while (!queryResult.isDone()) {
				System.out.println(queryResult);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			final SObject sObject = queryResult.getRecords()[0];
			final Organization organization = toOrganization(sObject);
			updateCredentals(organizationId, credentals);
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
		System.out.println(sObject);
		final Organization organization = new Organization();
		organization.setId(sObject.getId());
		organization.setName((String) sObject.getField("Name"));
		return organization;
	}
}
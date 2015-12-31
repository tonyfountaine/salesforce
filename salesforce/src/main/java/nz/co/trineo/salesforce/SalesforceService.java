package nz.co.trineo.salesforce;

import static com.sforce.soap.metadata.RetrieveStatus.Failed;
import static com.sforce.soap.metadata.RetrieveStatus.Succeeded;
import static org.apache.commons.io.FileUtils.iterateFiles;
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

import nz.co.trineo.common.CredentalsDAO;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.salesforce.model.Backup;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.Package;
import com.sforce.soap.metadata.PackageTypeMembers;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectorConfig;

public class SalesforceService {
	private static final Log log = LogFactory.getLog(SalesforceService.class);
	private static final double API_VERSION = 35.0;
	private static final long ONE_SECOND = 1000;
	private static final int MAX_NUM_POLL_REQUESTS = 50;
	private final CredentalsDAO dao;
	private final AppConfiguration configuration;

	public SalesforceService(final CredentalsDAO dao,
			final AppConfiguration configuration) throws IOException {
		this.dao = dao;
		this.configuration = configuration;
		FileUtils.forceMkdir(configuration.getSalesforceDirectory());
	}

	public Credentals currentCredentals() {
		return dao.get("salesforce");
	}

	public Credentals updateCredentals(final Credentals credentals) {
		credentals.setId("salesforce");
		dao.persist(credentals);
		return currentCredentals();
	}

	public void downloadAllMetadata(final String endPoint)
			throws SalesforceException {
		final Backup backup = createBackup(endPoint);
		try {
			extractMetadataZip(downloadBackup(backup.getDate()));
		} catch (IOException | ArchiveException e) {
			throw new SalesforceException(e);
		}
	}

	public Backup createBackup(final String endPoint)
			throws SalesforceException {
		final MetadataConnection connection = getMetadataConnection(endPoint);

		final DescribeMetadataObject[] metadata = describeMetadata(connection);

		final RetrieveRequest request = createRequest(metadata);
		try {
			final AsyncResult asyncResult = connection.retrieve(request);
			final RetrieveResult result = waitForRetrieveCompletion(connection,
					asyncResult);
			if (result.getStatus() == Failed) {
				throw new SalesforceException("code: "
						+ result.getErrorStatusCode() + ", message: "
						+ result.getErrorMessage());
			} else if (result.getStatus() == Succeeded) {
				final DateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd-HH-mm-ss");
				final Backup backup = new Backup();
				backup.setDate(format.format(new Date()));
				final String filename = backup.getDate() + ".zip";
				final File file = new File(configuration.getBackupDirectory(),
						filename);
				FileUtils.forceMkdir(file.getParentFile());
				try (InputStream in = new ByteArrayInputStream(
						result.getZipFile());
						OutputStream out = new FileOutputStream(file)) {
					IOUtils.copy(in, out);
				}
				return backup;
			}
		} catch (SalesforceException e) {
			throw e;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
		return null;
	}

	public InputStream downloadBackup(final String date)
			throws SalesforceException {
		final String filename = date + ".zip";
		try {
			final File file = new File(configuration.getBackupDirectory(),
					filename);
			log.info(file);
			log.info(file.exists());
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new SalesforceException(e);
		}
	}

	private void extractMetadataZip(final InputStream zipIn)
			throws IOException, FileNotFoundException, ArchiveException {
		try (ArchiveInputStream in = new ArchiveStreamFactory()
				.createArchiveInputStream(ArchiveStreamFactory.ZIP, zipIn);) {
			ZipArchiveEntry entry;
			while ((entry = (ZipArchiveEntry) in.getNextEntry()) != null) {
				final File destFile = new File(
						configuration.getSalesforceDirectory(), entry.getName());
				FileUtils.forceMkdir(destFile.getParentFile());
				try (OutputStream out = new FileOutputStream(destFile);) {
					IOUtils.copy(in, out);
				}
			}
		}
	}

	private DescribeMetadataObject[] describeMetadata(
			final MetadataConnection connection) throws SalesforceException {
		try {
			final DescribeMetadataResult result = connection
					.describeMetadata(API_VERSION);
			return result.getMetadataObjects();

		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private RetrieveResult waitForRetrieveCompletion(
			final MetadataConnection connection, AsyncResult asyncResult)
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
				throw new Exception(
						"Request timed out.  If this is a large set "
								+ "of metadata components, check that the time allowed "
								+ "by MAX_NUM_POLL_REQUESTS is sufficient.");
			}
			result = connection.checkRetrieveStatus(asyncResultId, true);
			System.out.println("Retrieve Status: " + result.getStatus());
		} while (!result.isDone());

		return result;
	}

	private RetrieveRequest createRequest(
			final DescribeMetadataObject[] metadata) {
		final RetrieveRequest request = new RetrieveRequest();
		request.setApiVersion(API_VERSION);
		request.setUnpackaged(createPackage(metadata));
		return request;
	}

	private ConnectorConfig createConfig(final String endpoint)
			throws FileNotFoundException {
		final ConnectorConfig config = new ConnectorConfig();
		config.setAuthEndpoint(endpoint);
		config.setServiceEndpoint(endpoint);
		config.setTraceFile("trace.log");
		config.setTraceMessage(true);
		config.setPrettyPrintXml(true);
		config.setManualLogin(true);
		return config;
	}

	private PartnerConnection getPartnerConnection(final ConnectorConfig config)
			throws SalesforceException {
		try {
			final PartnerConnection connection = new PartnerConnection(config);
			return connection;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private LoginResult login(final PartnerConnection connection)
			throws SalesforceException {
		try {
			final Credentals credentals = currentCredentals();
			final LoginResult result = connection.login(
					credentals.getUsername(), credentals.getPassword());
			return result;
		} catch (Exception e) {
			throw new SalesforceException(e);
		}
	}

	private MetadataConnection getMetadataConnection(final String endPoint)
			throws SalesforceException {
		try {
			final ConnectorConfig config = createConfig(endPoint);
			final PartnerConnection connection = getPartnerConnection(config);
			// QueryResult queryResult =
			// connection.query("select Id, Name from Orginization");
			// SObject sObject = queryResult.getRecords()[0];
			// String name = (String) sObject.getField("Name");
			final LoginResult result = login(connection);

			config.setServiceEndpoint(result.getMetadataServerUrl());
			config.setSessionId(result.getSessionId());
			return new MetadataConnection(config);
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

	@SuppressWarnings("unchecked")
	public List<Backup> listBackups() {
		final List<Backup> backups = new ArrayList<>();
		final Iterator<File> files = iterateFiles(
				configuration.getBackupDirectory(), new String[] { "zip" },
				false);
		for (; files.hasNext();) {
			final File file = files.next();
			final Backup backup = new Backup();
			final String baseName = getBaseName(file.getName());
			backup.setDate(baseName);
			backups.add(backup);
		}
		return backups;
	}
}

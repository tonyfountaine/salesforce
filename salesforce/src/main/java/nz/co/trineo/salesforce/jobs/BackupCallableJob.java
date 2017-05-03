package nz.co.trineo.salesforce.jobs;

import static com.sforce.soap.metadata.RetrieveStatus.Failed;
import static com.sforce.soap.metadata.RetrieveStatus.Succeeded;
import static java.text.MessageFormat.format;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.ws.ConnectionException;

import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.git.GitService;
import nz.co.trineo.salesforce.SalesforceException;
import nz.co.trineo.salesforce.SalesforceService;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.BackupStatus;
import nz.co.trineo.salesforce.model.Organization;

public final class BackupCallableJob implements Callable<Void> {
	private static final Log log = LogFactory.getLog(SalesforceService.class);
	private static final long ONE_SECOND = 1000;
	private static final int MAX_NUM_POLL_REQUESTS = 50;

	private final Organization org;
	private final Backup backup;
	private final File salesforceDir;
	private final SessionFactory sessionFactory;
	private final SalesforceService service;
	private final GitService gitService;

	public BackupCallableJob(Organization org, Backup backup, final File salesforceDir,
			final SessionFactory sessionFactory, final SalesforceService service, final GitService gitService) {
		this.org = org;
		this.backup = backup;
		this.salesforceDir = salesforceDir;
		this.sessionFactory = sessionFactory;
		this.service = service;
		this.gitService = gitService;
	}

	@Override
	public Void call() throws Exception {
		final Session session = sessionFactory.openSession();
		try {
			ManagedSessionContext.bind(session);
			final Transaction transaction = session.beginTransaction();
			try {
				try {
					final RetrieveResult result = waitForRetrieveCompletion(org, backup.getRetrieveId());
					if (result.getStatus() == Failed) {
						throw new SalesforceException(format("code: {0}, message: {1}", result.getErrorStatusCode(),
								result.getErrorMessage()));
					} else if (result.getStatus() == Succeeded) {
						backup.setStatus(BackupStatus.SUCCESSFUL);
						final File repoDir = new File(salesforceDir, org.getId());
						cleanDirectory(repoDir);
						try (InputStream in = new ByteArrayInputStream(result.getZipFile())) {
							extractMetadataZip(repoDir, in);
						}
						gitService.commit(repoDir, format("Backup of all metadata for {0}. timestamp: {1}",
								org.getName(), backup.getName()));
						gitService.tag(repoDir, backup.getName());
					}
				} catch (final Exception e) {
					backup.setStatus(BackupStatus.FAILED);
					log.error("Unable to retrieve backup", e);
				} finally {
					service.updateBackup(backup);
				}
				transaction.commit();
			} catch (final Exception e) {
				transaction.rollback();
			}
		} finally {
			session.close();
			ManagedSessionContext.unbind(sessionFactory);
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
	 * @param connection
	 * @param asyncResult
	 * @return
	 * @throws InterruptedException
	 * @throws SalesforceException
	 * @throws ConnectionException
	 */
	private RetrieveResult waitForRetrieveCompletion(final Organization org, final String asyncResultId)
			throws SalesforceException, InterruptedException, ConnectionException {
		final ConnectedAccount account = org.getAccount();

		// Wait for the retrieve to complete
		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND;
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
			result = service.checkRetrieveStatus(account, asyncResultId, true);
			log.info("Retrieve Status: " + result.getStatus());
		} while (!result.isDone());

		return result;
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
}
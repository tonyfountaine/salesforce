package nz.co.trineo.salesforce.jobs;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import com.google.common.collect.ImmutableMultimap;

import io.dropwizard.servlets.tasks.Task;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.GitServiceException;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.BackupStatus;
import nz.co.trineo.salesforce.model.OrganizationDAO;

public class RefreshBackupsTask extends Task {
	private static final Pattern DATE_PATTERN = Pattern.compile(".*\\s+(\\d+-\\d+-\\d+-\\d+-\\d+-\\d+).*");
	private final OrganizationDAO orgDAO;
	private final GitService gitService;
	private final AppConfiguration configuration;

	private final SessionFactory sessionFactory;

	public RefreshBackupsTask(final OrganizationDAO orgDAO, final GitService gitService,
			final AppConfiguration configuration, final SessionFactory sessionFactory) {
		super("refreshBackups");
		this.orgDAO = orgDAO;
		this.gitService = gitService;
		this.configuration = configuration;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void execute(final ImmutableMultimap<String, String> parameters, final PrintWriter output) throws Exception {
		final Session session = sessionFactory.openSession();
		try {
			ManagedSessionContext.bind(session);
			final Transaction transaction = session.beginTransaction();
			try {
				orgDAO.listAll().forEach(o -> {
					final String id = o.getId();
					final List<Backup> backups = new ArrayList<>();
					final File repoDir = new File(configuration.getSalesforceDirectory(), id);
					try {
						final List<String> logEntries = gitService.log(repoDir);
						logEntries.forEach(l -> {
							final Matcher matcher = DATE_PATTERN.matcher(l);
							if (matcher.find()) {
								final String date = matcher.group(1);
								final Backup b = new Backup();
								b.setName(date);
								b.setStatus(BackupStatus.SUCCESSFUL);
								backups.add(b);
							}
						});
					} catch (final GitServiceException e) {
						e.printStackTrace();
					}
					o.setBackups(backups);
					orgDAO.persist(o);
				});
				transaction.commit();
			} catch (final Exception e) {
				transaction.rollback();
				throw new RuntimeException(e);
			}
		} finally {
			session.close();
			ManagedSessionContext.unbind(sessionFactory);
		}
	}
}

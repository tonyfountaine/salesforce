package nz.co.trineo.git;

import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.git.model.GitTask;

public class GitService {
	private static final Log log = LogFactory.getLog(GitService.class);

	public static final class GitMonitor implements ProgressMonitor {

		private final GitProcess process;
		private final GitProcessDAO processDAO;

		public GitMonitor(GitProcess process, final GitProcessDAO processDAO) {
			super();
			this.process = process;
			this.processDAO = processDAO;
		}

		@Override
		public void update(int completed) {
			process.getTask().setCurrentWork(completed);
			processDAO.persist(process);
		}

		@Override
		public void start(int totalTasks) {
			process.setTotalTasks(totalTasks);
			processDAO.persist(process);
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public void endTask() {
			process.setTask(null);
			process.setCompletedTasks(process.getCompletedTasks() + 1);
			processDAO.persist(process);
		}

		@Override
		public void beginTask(String title, int totalWork) {
			process.setTask(new GitTask(title, totalWork));
			processDAO.persist(process);
		}
	}

	private final AppConfiguration configuration;
	private final GitProcessDAO processDAO;
	private final AccountDAO accountDAO;

	public GitService(AppConfiguration configuration, final GitProcessDAO processDAO, final AccountDAO accountDAO)
			throws IOException {
		super();
		this.configuration = configuration;
		this.processDAO = processDAO;
		this.accountDAO = accountDAO;
		forceMkdir(configuration.getGitDirectory());
	}

	public GitProcess clone(String name, String cloneURL, final int accId) throws GitServiceException {
		final ConnectedAccount account = accountDAO.get(accId);
		final GitProcess process = new GitProcess();
		processDAO.persist(process);
		final GitMonitor monitor = new GitMonitor(process, processDAO);
		final UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
				account.getCredentals().getUsername(), account.getCredentals().getPassword());
		try (Git git = Git.cloneRepository().setDirectory(new File(configuration.getGitDirectory(), name))
				.setURI(cloneURL).setProgressMonitor(monitor).setCredentialsProvider(credentialsProvider).call();) {
			git.getRepository().close();
		} catch (GitAPIException e) {
			throw new GitServiceException(e);
		}
		return process;
	}

	public GitProcess getProcess(final int id) {
		return processDAO.get(id);
	}
}

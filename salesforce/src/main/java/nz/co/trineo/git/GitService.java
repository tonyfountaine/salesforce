package nz.co.trineo.git;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;

import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.git.model.GitTask;

public class GitService {

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

	public GitService(AppConfiguration configuration, final GitProcessDAO processDAO) {
		super();
		this.configuration = configuration;
		this.processDAO = processDAO;
	}

	public GitProcess clone(String name, String cloneURL) throws GitServiceException {
		final GitProcess process = new GitProcess();
		processDAO.persist(process);
		final GitMonitor monitor = new GitMonitor(process, processDAO);
		try (Git git = Git.cloneRepository().setDirectory(new File(configuration.getGitDirectory(), name))
				.setURI(cloneURL).setProgressMonitor(monitor).call();) {
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

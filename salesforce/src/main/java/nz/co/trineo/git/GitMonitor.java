package nz.co.trineo.git;

import org.eclipse.jgit.lib.ProgressMonitor;

import nz.co.trineo.model.GitProcess;
import nz.co.trineo.model.GitTask;

public final class GitMonitor implements ProgressMonitor {

	private final GitProcess process;
	private final GitProcessDAO processDAO;

	public GitMonitor(final GitProcess process, final GitProcessDAO processDAO) {
		super();
		this.process = process;
		this.processDAO = processDAO;
	}

	@Override
	public void beginTask(final String title, final int totalWork) {
		process.setTask(new GitTask(title, totalWork));
		processDAO.persist(process);
	}

	@Override
	public void endTask() {
		process.setTask(null);
		process.setCompletedTasks(process.getCompletedTasks() + 1);
		processDAO.persist(process);
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void start(final int totalTasks) {
		process.setTotalTasks(totalTasks);
		processDAO.persist(process);
	}

	@Override
	public void update(final int completed) {
		process.getTask().setCurrentWork(completed);
		processDAO.persist(process);
	}
}
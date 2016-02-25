package nz.co.trineo.git;

import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

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

		public GitMonitor(final GitProcess process, final GitProcessDAO processDAO) {
			super();
			this.process = process;
			this.processDAO = processDAO;
		}

		@Override
		public void update(final int completed) {
			process.getTask().setCurrentWork(completed);
			processDAO.persist(process);
		}

		@Override
		public void start(final int totalTasks) {
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
		public void beginTask(final String title, final int totalWork) {
			process.setTask(new GitTask(title, totalWork));
			processDAO.persist(process);
		}
	}

	private final AppConfiguration configuration;
	private final GitProcessDAO processDAO;
	private final AccountDAO accountDAO;

	public GitService(final AppConfiguration configuration, final GitProcessDAO processDAO, final AccountDAO accountDAO)
			throws IOException {
		super();
		this.configuration = configuration;
		this.processDAO = processDAO;
		this.accountDAO = accountDAO;
		forceMkdir(configuration.getGitDirectory());
	}

	public GitProcess clone(final String name, final String cloneURL, final int accId) throws GitServiceException {
		final ConnectedAccount account = accountDAO.get(accId);
		final GitProcess process = new GitProcess();
		processDAO.persist(process);
		final GitMonitor monitor = new GitMonitor(process, processDAO);
		final UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
				account.getCredentals().getUsername(), account.getCredentals().getPassword());
		try (Git git = Git.cloneRepository().setDirectory(new File(configuration.getGitDirectory(), name))
				.setURI(cloneURL).setProgressMonitor(monitor).setCredentialsProvider(credentialsProvider).call();) {
			git.getRepository().close();
		} catch (final GitAPIException e) {
			throw new GitServiceException(e);
		}
		return process;
	}

	public GitProcess getProcess(final int id) {
		return processDAO.get(id);
	}

	public void createRepo(final File repoDir) throws GitServiceException {
		repoDir.mkdirs();
		final File gitDir = new File(repoDir, ".git");
		try (Git git = Git.init().setDirectory(repoDir).call();
				Repository repository = FileRepositoryBuilder.create(gitDir)) {
		} catch (IllegalStateException | GitAPIException | IOException e) {
			throw new GitServiceException(e);
		}
	}

	public boolean isRepo(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir)) {
			return true;
		} catch (final IOException e) {
			log.error("Not a valid Git Repo", e);
		}
		return false;
	}

	public Set<String> getTags(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir)) {
			final Map<String, Ref> tags = repository.getTags();
			return tags.keySet();
		} catch (final IOException e) {
			throw new GitServiceException(e);
		}
	}

	public List<String> removeTag(final File repoDir, final String tag) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			return git.tagDelete().setTags(tag).call();
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public void checkout(final File repoDir, final String name) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			git.checkout().setName(name).call();
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public void commit(final File repoDir, final String message) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			git.add().addFilepattern(".").call();
			git.commit().setMessage(message).call();
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public void tag(final File repoDir, final String date) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			git.tag().setName(date).setAnnotated(true).call();
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public List<String> diff(final File repoDir, final String firstTag, final String secondTag)
			throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir);) {
			final AbstractTreeIterator oldTreeIter = prepareTreeParser(repository, firstTag);
			final AbstractTreeIterator newTreeIter = prepareTreeParser(repository, secondTag);

			return diffTrees(repository, oldTreeIter, newTreeIter);
		} catch (IOException e) {
			throw new GitServiceException(e);
		}
	}

	public List<String> diffRepos(final File repoDirA, final File repoDirB) throws GitServiceException {
		final File gitDirA = new File(repoDirA, ".git");
		final File gitDirB = new File(repoDirB, ".git");
		try (Repository repositoryA = FileRepositoryBuilder.create(gitDirA);
				Repository repositoryB = FileRepositoryBuilder.create(gitDirB);) {
			final AbstractTreeIterator oldTreeIter = prepareTreeParser(repositoryA, "refs/heads/master");
			final AbstractTreeIterator newTreeIter = prepareTreeParser(repositoryB, "refs/heads/master");

			return diffTrees(repositoryA, oldTreeIter, newTreeIter);
		} catch (IOException e) {
			throw new GitServiceException(e);
		}
	}

	private List<String> diffTrees(Repository repository, final AbstractTreeIterator oldTreeIter,
			final AbstractTreeIterator newTreeIter) throws IOException, UnsupportedEncodingException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final List<String> diffList = new ArrayList<>();
		try (final DiffFormatter df = new DiffFormatter(out);) {
			df.setRepository(repository);
			df.setNewPrefix("");
			df.setOldPrefix("");
			df.format(oldTreeIter, newTreeIter);
			diffList.add(out.toString("UTF-8"));
		}

		return diffList;
	}

	private AbstractTreeIterator prepareTreeParser(final Repository repository, final String ref)
			throws IOException, MissingObjectException, IncorrectObjectTypeException {
		// from the commit we can build the tree which allows us to construct the TreeParser
		final Ref head = repository.getRef(ref);
		try (RevWalk walk = new RevWalk(repository)) {
			final RevCommit commit = walk.parseCommit(head.getObjectId());
			final RevTree tree = walk.parseTree(commit.getTree().getId());

			final CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
			try (ObjectReader oldReader = repository.newObjectReader()) {
				oldTreeParser.reset(oldReader, tree.getId());
			}

			walk.dispose();

			return oldTreeParser;
		}
	}
}

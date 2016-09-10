package nz.co.trineo.git;

import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import nz.co.trineo.common.Service;
import nz.co.trineo.common.model.Credentals;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.git.model.GitProcess;
import nz.co.trineo.git.model.GitRepo;

public class GitService implements Service {
	private static final String ORIGIN = "origin";
	private static final Log log = LogFactory.getLog(GitService.class);

	private final AppConfiguration configuration;
	private final GitProcessDAO processDAO;
	private final GitRepoDAO gitRepoDAO;

	public GitService(final AppConfiguration configuration, final GitProcessDAO processDAO, final GitRepoDAO gitRepoDAO)
			throws IOException {
		super();
		this.configuration = configuration;
		this.processDAO = processDAO;
		this.gitRepoDAO = gitRepoDAO;
		forceMkdir(configuration.getGitDirectory());
	}

	public List<GitRepo> listRepos() throws GitServiceException {
		return gitRepoDAO.listAll();
		/* final List<GitRepo> repos = new ArrayList<>(); final Path gitDirectory =
		 * configuration.getGitDirectory().toPath(); try { Files.walk(gitDirectory, 1).filter(p ->
		 * Files.isDirectory(p.resolve(".git"))).forEach(p -> { try { final String remote = getRemote(p.toFile()); final
		 * GitRepo repo = new GitRepo(); repo.setName(p.getFileName().toString()); repo.setRemote(remote);
		 *
		 * repos.add(repo); } catch (final GitServiceException e) { log.error(e); } }); } catch (final IOException e) {
		 * log.error(e); } return repos; */
	}

	public GitProcess clone(final String name, final String cloneURL, final String username, final String password)
			throws GitServiceException {
		final GitRepo gitRepo = new GitRepo();
		gitRepo.setName(name);
		gitRepo.setRemote(cloneURL);
		gitRepo.setCredentals(new Credentals());
		gitRepo.getCredentals().setPassword(password);
		gitRepo.getCredentals().setUsername(username);
		final GitProcess process = new GitProcess();
		processDAO.persist(process);
		final GitMonitor monitor = new GitMonitor(process, processDAO);
		final UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
				gitRepo.getCredentals().getUsername(), gitRepo.getCredentals().getPassword());
		final File repoDir = new File(configuration.getGitDirectory(), name);
		try (Git git = Git.cloneRepository().setDirectory(repoDir).setURI(cloneURL).setProgressMonitor(monitor)
				.setCredentialsProvider(credentialsProvider).call();) {
			git.getRepository().close();
		} catch (final GitAPIException e) {
			throw new GitServiceException(e);
		}
		gitRepoDAO.persist(gitRepo);
		return process;
	}

	public GitProcess getProcess(final int id) {
		return processDAO.get(id);
	}

	public void createRepo(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		createRepo(repoDir);
	}

	public void createRepo(final File repoDir) throws GitServiceException {
		final GitRepo gitRepo = new GitRepo();
		gitRepo.setName(repoDir.getName());
		if (!isRepo(repoDir)) {
			try {
				Files.createDirectory(repoDir.toPath());
			} catch (final IOException e) {
				throw new GitServiceException(e);
			}
			final File gitDir = new File(repoDir, ".git");
			try (Git git = Git.init().setDirectory(repoDir).call();
					Repository repository = FileRepositoryBuilder.create(gitDir)) {
			} catch (IllegalStateException | GitAPIException | IOException e) {
				throw new GitServiceException(e);
			}
		}
		gitRepoDAO.persist(gitRepo);
	}

	public boolean isRepo(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return isRepo(repoDir);
	}

	public boolean isRepo(final File repoDir) throws GitServiceException {
		if (Files.exists(repoDir.toPath())) {
			final File gitDir = new File(repoDir, ".git");
			try (Repository repository = FileRepositoryBuilder.create(gitDir)) {
				return true;
			} catch (final IOException e) {
				log.error("Not a valid Git Repo", e);
			}
		}
		return false;
	}

	public List<String> getTags(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return getTags(repoDir);
	}

	public List<String> getTags(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			final List<Ref> refs = git.tagList().call();
			final List<String> tags = new ArrayList<>();
			refs.forEach(r -> {
				tags.add(r.getName().replace("refs/tags/", ""));
			});
			return tags;
		} catch (final IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public List<String> removeTag(final String name, final String tag) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return removeTag(repoDir, tag);
	}

	public List<String> removeTag(final File repoDir, final String tag) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			return git.tagDelete().setTags(tag).call();
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public void checkout(final String name, final String branch) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		checkout(repoDir, branch);
	}

	public void checkout(final File repoDir, final String name) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			git.checkout().setName(name).call();
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public void commit(final String name, final String message) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		commit(repoDir, message);
	}

	public void commit(final File repoDir, final String message) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			git.add().addFilepattern(".").call();
			git.commit().setAll(true).setMessage(message).call();
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public void tag(final String name, final String tag) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		tag(repoDir, tag);
	}

	public void tag(final File repoDir, final String tag) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			git.tag().setName(tag).setAnnotated(true).call();
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public List<GitDiff> diff(final String name, final String firstTag, final String secondTag)
			throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return diff(repoDir, firstTag, secondTag);
	}

	public List<GitDiff> diff(final File repoDir, final String firstTag, final String secondTag)
			throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir);) {
			final AbstractTreeIterator oldTreeIter = prepareTreeParser(repository, firstTag);
			final AbstractTreeIterator newTreeIter = prepareTreeParser(repository, secondTag);

			return diffTrees(repository, oldTreeIter, newTreeIter);
		} catch (final IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public List<GitDiff> diffRepos(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return diffRepos(repoDir);
	}

	public List<GitDiff> diffRepos(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repo = FileRepositoryBuilder.create(gitDir);) {
			final ObjectId fetchHead = repo.resolve("FETCH_HEAD^{tree}");
			final ObjectId head = repo.resolve("HEAD^{tree}");
			final ObjectReader reader = repo.newObjectReader();
			final CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, fetchHead);
			final CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, head);

			return diffTrees(repo, oldTreeIter, newTreeIter);
		} catch (final IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public String getRemote(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return getRemote(repoDir);
	}

	public String getRemote(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir);) {
			final StoredConfig config = repository.getConfig();
			final String remoteURL = config.getString("remote", ORIGIN, "url");
			return remoteURL;
		} catch (final IOException e) {
			throw new GitServiceException(e);
		}
	}

	public void fetchRemote(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		fetchRemote(repoDir);
	}

	public void fetchRemote(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			final RefSpec refSpec = new RefSpec("refs/heads/master:refs/remotes/" + ORIGIN + "/master");
			final FetchResult result = git.fetch().setCheckFetchedObjects(true).setRefSpecs(refSpec).call();// setRemote(ORIGIN).call();
			log.info(result.getMessages());
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	public void addRemote(final String nameA, final String nameB) throws GitServiceException {
		final File repoDirA = new File(configuration.getGitDirectory(), nameA);
		final File repoDirB = new File(configuration.getGitDirectory(), nameB);
		addRemote(repoDirA, repoDirB);
	}

	public void addRemote(final File repoDirA, final File repoDirB) throws GitServiceException {
		final File gitDirA = new File(repoDirA, ".git");
		final File gitDirB = new File(repoDirB, ".git");
		try (Repository repositoryA = FileRepositoryBuilder.create(gitDirA);) {
			final StoredConfig config = repositoryA.getConfig();
			config.setString("remote", ORIGIN, "url", gitDirB.getAbsolutePath());
			config.save();
		} catch (final IOException e) {
			throw new GitServiceException(e);
		}
	}

	public boolean isRemote(final String nameA, final String nameB) throws GitServiceException {
		final File repoDirA = new File(configuration.getGitDirectory(), nameA);
		final File repoDirB = new File(configuration.getGitDirectory(), nameB);
		return isRemote(repoDirA, repoDirB);
	}

	public boolean isRemote(final File repoDirA, final File repoDirB) throws GitServiceException {
		final File gitDirA = new File(repoDirA, ".git");
		final File gitDirB = new File(repoDirB, ".git");
		try (Repository repositoryA = FileRepositoryBuilder.create(gitDirA);) {
			final StoredConfig config = repositoryA.getConfig();
			final String remoteURL = config.getString("remote", ORIGIN, "url");
			return gitDirB.getAbsolutePath().equals(remoteURL);
		} catch (final IOException e) {
			throw new GitServiceException(e);
		}
	}

	public void removeRemote(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		removeRemote(repoDir);
	}

	public void removeRemote(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir);) {
			final StoredConfig config = repository.getConfig();
			config.unsetSection("remote", ORIGIN);
			config.save();
		} catch (final IOException e) {
			throw new GitServiceException(e);
		}
	}

	public List<String> log(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return log(repoDir);
	}

	public List<String> log(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		final List<String> logEntries = new ArrayList<>();
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			final Iterable<RevCommit> log = git.log().call();
			log.forEach(r -> {
				logEntries.add(r.getFullMessage());
			});
		} catch (final IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
		return logEntries;
	}

	private List<GitDiff> diffTrees(final Repository repository, final AbstractTreeIterator oldTreeIter,
			final AbstractTreeIterator newTreeIter) throws GitAPIException, IOException {
		try (final Git git = new Git(repository);
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final GitDiffFormatter df = new GitDiffFormatter(out);) {
			final List<DiffEntry> list = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
			df.setRepository(repository);
			list.forEach(d -> {
				try {
					log.info(d);
					df.setPaths(d.getNewPath(), d.getOldPath());
					df.format(d);
					out.reset();
				} catch (final Exception e) {
					log.error("An error ocurred while processing an entry", e);
				}
			});
			return df.getEntries();
		}
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

	public List<String> branches(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return branches(repoDir);
	}

	public List<String> branches(final File repoDir) throws GitServiceException {
		final File gitDir = new File(repoDir, ".git");
		final List<String> branches = new ArrayList<>();
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			final List<Ref> list = git.branchList().setListMode(ListMode.ALL).call();
			list.forEach(r -> {
				final String name = r.getName();
				branches.add(name);
			});
			return branches;
		} catch (final IOException | GitAPIException e) {
			throw new GitServiceException(e);
		}
	}

	@Override
	public String getName() {
		return "Git";
	}

	public void credentals(final String name, final String username, final String password) {
		final GitRepo gitRepo = gitRepoDAO.getByName(name);
		if (gitRepo.getCredentals() == null) {
			gitRepo.setCredentals(new Credentals());
		}
		final Credentals credentals = gitRepo.getCredentals();
		credentals.setPassword(password);
		credentals.setUsername(username);
		gitRepoDAO.persist(gitRepo);
	}

	public PullResult pull(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return pull(repoDir);
	}

	public PullResult pull(final File repoDir) throws GitServiceException {
		final GitRepo gitRepo = gitRepoDAO.getByName(repoDir.getName());
		final UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
				gitRepo.getCredentals().getUsername(), gitRepo.getCredentals().getPassword());
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			final PullResult call = git.pull().setCredentialsProvider(credentialsProvider)
					.setRemote(gitRepo.getRemote()).call();
			return call;
		} catch (final GitAPIException | IOException e) {
			throw new GitServiceException(e);
		}
	}

	public Iterable<PushResult> push(final String name) throws GitServiceException {
		final File repoDir = new File(configuration.getGitDirectory(), name);
		return push(repoDir);
	}

	public Iterable<PushResult> push(final File repoDir) throws GitServiceException {
		final GitRepo gitRepo = gitRepoDAO.getByName(repoDir.getName());
		final UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
				gitRepo.getCredentals().getUsername(), gitRepo.getCredentals().getPassword());
		final File gitDir = new File(repoDir, ".git");
		try (Repository repository = FileRepositoryBuilder.create(gitDir); Git git = new Git(repository);) {
			final Iterable<PushResult> call = git.push().setCredentialsProvider(credentialsProvider)
					.setRemote(gitRepo.getRemote()).call();
			return call;
		} catch (final GitAPIException | IOException e) {
			throw new GitServiceException(e);
		}
	}
}

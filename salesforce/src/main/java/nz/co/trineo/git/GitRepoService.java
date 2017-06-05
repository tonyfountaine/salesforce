package nz.co.trineo.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ClientService;
import nz.co.trineo.common.Service;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.github.GitHubService;
import nz.co.trineo.model.Branch;
import nz.co.trineo.model.Client;
import nz.co.trineo.model.ConnectedAccount;
import nz.co.trineo.model.Credentals;
import nz.co.trineo.model.GitDiff;
import nz.co.trineo.model.Repository;
import nz.co.trineo.model.RepositoryType;
import nz.co.trineo.model.Tag;
import nz.co.trineo.model.TreeNode;
import nz.co.trineo.repo.RepoDAO;
import nz.co.trineo.repo.RepoService;

public class GitRepoService implements Service, RepoService {
	private static final Log log = LogFactory.getLog(GitHubService.class);
	private static final Pattern urlPatten = Pattern.compile("github.com/(.*)/(.*)\\.git");

	private final RepoDAO dao;
	private final AccountDAO credDAO;
	private final AppConfiguration configuration;
	private final ClientService clientService;
	private final GitService gitService;

	public GitRepoService(final RepoDAO dao, final AppConfiguration configuration, final AccountDAO credDAO,
			final ClientService clientService, final GitService gitService) {
		this.dao = dao;
		this.configuration = configuration;
		this.credDAO = credDAO;
		this.clientService = clientService;
		this.gitService = gitService;
	}

	private void addNode(final Map<String, TreeNode> nodeMap, final String path, final TreeNode node) {
		if (nodeMap.containsKey(path)) {
			return;
		}
		String parentPath = new File(path).getParent();
		if (StringUtils.isBlank(parentPath)) {
			parentPath = "/";
		}

		final TreeNode parentNode;
		if (nodeMap.containsKey(parentPath)) {
			parentNode = nodeMap.get(parentPath);
		} else {
			final File parentFile = new File(parentPath);
			parentNode = new TreeNode();
			parentNode.setText(parentFile.getName());
			addNode(nodeMap, parentPath, parentNode);
		}
		parentNode.getNodes().add(node);
		nodeMap.put(path, node);
	}

	public void checkout(final int id, final String name) throws GitServiceException {
		final Repository repository = dao.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			gitService.checkout(repoDir, name);
		} catch (final GitServiceException e) {
			throw new GitServiceException(e);
		}
	}

	public void clone(final int id) throws GitServiceException {
		final Repository repository = dao.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			gitService.clone(repoDir, repository.getCloneURL(), repository.getAccount().getToken().getAccessToken(),
					new char[0]);
		} catch (final GitServiceException e) {
			throw new GitServiceException(e);
		}
	}

	public Repository createRepo(final String repoURL, final int accId) throws GitServiceException {
		log.info("repoURL: " + repoURL + ", accId: " + accId);
		final Repository repository = new Repository();
		final ConnectedAccount account = credDAO.get(accId);
		final Credentals credentals = account.getCredentals();

		repository.setAccount(account);
		repository.setCloneURL(repoURL);
		repository.setType(RepositoryType.git);
		final Matcher matcher = urlPatten.matcher(repoURL);
		if (matcher.find()) {
			final String name = matcher.group(2);
			gitService.clone(name, repoURL, credentals.getUsername(), credentals.getPassword().toCharArray());
		}

		dao.persist(repository);
		return repository;
	}

	public void deleteRepo(final int id) {
		dao.delete(id);
	}

	public List<GitDiff> diffBranches(final long id, final long compareId) throws GitServiceException {
		final Branch branch = dao.getBranch(id);
		final Branch branch2 = dao.getBranch(compareId);
		final Repository repo = branch.getRepo();
		final File repoDir = new File(configuration.getGithubDirectory(), repo.getName());
		if (!isRepo(repo.getId())) {
			clone(repo.getId());
		}
		pull(repo.getId());
		try {
			return gitService.diff(repoDir, branch.getName(), branch2.getName(), null);
		} catch (final GitServiceException e) {
			throw new GitServiceException(e);
		}
	}

	public List<Branch> getBranches(final int id) throws GitServiceException {
		final Repository repository = dao.get(id);
		log.info(repository);
		if (repository.getBranches() == null || repository.getBranches().isEmpty()) {
			return updateBranches(id);
		}
		return repository.getBranches();
	}

	public Branch createBranch(final int id, final String branchName) throws GitServiceException {
		final Branch branch = dao.getBranch(id);
		final Repository repository = branch.getRepo();
		final Branch newBranch = new Branch();
		newBranch.setName(branchName);
		newBranch.setOrg(branch.getOrg());
		newBranch.setRepo(repository);
		repository.getBranches().add(newBranch);
		gitService.createBranch(repository.getName(), branchName);
		dao.persist(repository);
		return newBranch;
	}

	public RepositoryCommit getCommit(final int id, final String sha1) throws GitServiceException {
		final Repository repository = dao.get(id);
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		if (matcher.find()) {
			final String user = matcher.group(1);
			final String name = matcher.group(2);
			final CommitService service = new CommitService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				return service.getCommit(RepositoryId.create(user, name), sha1);
			} catch (final IOException e) {
				throw new GitServiceException(e);
			}
		}
		return null;
	}

	public List<String> getCommits(final int id) throws GitServiceException {
		final List<String> list = new ArrayList<>();
		final Repository repository = dao.get(id);
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		if (matcher.find()) {
			final String user = matcher.group(1);
			final String name = matcher.group(2);
			final CommitService service = new CommitService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				final List<RepositoryCommit> tags = service.getCommits(RepositoryId.create(user, name));
				tags.forEach(t -> {
					list.add(t.getSha());
				});
			} catch (final IOException e) {
				throw new GitServiceException(e);
			}
		}
		return list;
	}

	public TreeNode getDiffTree(final List<GitDiff> diffs) throws GitServiceException {
		final Map<String, TreeNode> nodeMap = new HashMap<>();

		final TreeNode rootNode = new TreeNode();
		rootNode.setText("/");
		rootNode.getState().setExpanded(true);

		nodeMap.put(rootNode.getText(), rootNode);

		diffs.forEach(d -> {
			final String path = "/dev/null".equals(d.getPathA()) ? d.getPathB() : d.getPathA();
			final File file = new File(path);
			final TreeNode node = new TreeNode();
			node.setText(file.getName());
			addNode(nodeMap, path, node);
		});

		return rootNode;
	}

	@Override
	public String getName() {
		return "Git";
	}

	public Repository getRepo(final int id) throws GitServiceException {
		final Repository repository = dao.get(id);
		log.debug(repository);
		return repository;
	}

	public List<Repository> getRepos() throws GitServiceException {
		return dao.listByType(RepositoryType.git);
	}

	public List<Tag> getTags(final int id) throws GitServiceException {
		final Repository repository = dao.get(id);
		if (repository.getTags() == null || repository.getTags().isEmpty()) {
			return updateTags(id);
		}
		return repository.getTags();
	}

	public boolean isRepo(final int id) throws GitServiceException {
		final Repository repository = dao.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			return gitService.isRepo(repoDir);
		} catch (final GitServiceException e) {
			throw new GitServiceException(e);
		}
	}

	public void pull(final int id) throws GitServiceException {
		final Repository repository = dao.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			gitService.pull(repoDir, repository.getAccount().getToken().getAccessToken(), new char[0]);
		} catch (final GitServiceException e) {
			throw new GitServiceException(e);
		}
	}

	public Branch readBranch(final long id) {
		return dao.getBranch(id);
	}

	public List<Branch> updateBranches(final int id) throws GitServiceException {
		final Repository repository = dao.get(id);
		final List<Branch> list = repository.getBranches();
		final Map<String, Branch> branchMap = new HashMap<>();
		list.forEach(b -> {
			branchMap.put(b.getName(), b);
		});
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		if (matcher.find()) {
			final String user = matcher.group(1);
			final String name = matcher.group(2);
			final RepositoryService service = new RepositoryService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				final List<RepositoryBranch> branches = service.getBranches(RepositoryId.create(user, name));
				branches.forEach(b -> {
					final Branch br;
					final String bName = b.getName();
					if (branchMap.containsKey(bName)) {
						br = branchMap.get(bName);
						br.setSha(b.getCommit().getSha());
					} else {
						br = new Branch();
						br.setName(bName);
						br.setUrl(b.getCommit().getType());
						br.setSha(b.getCommit().getSha());
						br.setRepo(repository);
						list.add(br);
					}
				});
			} catch (final IOException e) {
				throw new GitServiceException(e);
			}
		}
		repository.setBranches(list);
		dao.persist(repository);
		return list;
	}

	public Repository updateRepo(final Repository repo) {
		final Repository repository = dao.get(repo.getId());
		if (repo.getClient() != null) {
			final Client client = clientService.read(repo.getClient().getId());
			repository.setClient(client);
			if (!client.getRepositories().contains(repository)) {
				client.getRepositories().add(repository);
			}
			clientService.update(client);
		}
		dao.persist(repository);
		return repository;
	}

	public List<Tag> updateTags(final int id) throws GitServiceException {
		final List<Tag> list = new ArrayList<>();
		final Repository repository = dao.get(id);
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		if (matcher.find()) {
			final String user = matcher.group(1);
			final String name = matcher.group(2);
			final RepositoryService service = new RepositoryService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				final List<RepositoryTag> tags = service.getTags(RepositoryId.create(user, name));
				tags.forEach(t -> {
					final Tag tag = new Tag();
					tag.setName(t.getName());
					tag.setSha(t.getCommit().getSha());
					tag.setTarballUrl(t.getTarballUrl());
					tag.setUrl(t.getCommit().getUrl());
					tag.setZipballUrl(t.getZipballUrl());
					tag.setRepo(repository);
					list.add(tag);
				});
			} catch (final IOException e) {
				throw new GitServiceException(e);
			}
		}
		repository.setTags(list);
		dao.persist(repository);
		return list;
	}
}

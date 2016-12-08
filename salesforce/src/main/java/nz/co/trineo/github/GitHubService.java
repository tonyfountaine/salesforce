package nz.co.trineo.github;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ClientService;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.GitServiceException;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.github.model.Branch;
import nz.co.trineo.github.model.Repository;
import nz.co.trineo.github.model.Tag;
import nz.co.trineo.salesforce.model.TreeNode;

public class GitHubService implements ConnectedService {
	private static final Log log = LogFactory.getLog(GitHubService.class);
	private static final Pattern urlPatten = Pattern.compile("github.com/(.*)/(.*)\\.git");

	private final GitHubRepoDAO dao;
	private final AccountDAO credDAO;
	private final AppConfiguration configuration;
	private final ClientService clientService;
	private final GitService gitService;

	public GitHubService(final GitHubRepoDAO dao, final AppConfiguration configuration, final AccountDAO credDAO,
			final ClientService clientService, final GitService gitService) {
		this.dao = dao;
		this.configuration = configuration;
		this.credDAO = credDAO;
		this.clientService = clientService;
		this.gitService = gitService;
	}

	@Override
	public String getName() {
		return "GitHub";
	}

	public List<Repository> getRepos() throws GitHubServiceException {
		return dao.listAll();
	}

	public Repository getRepo(final int id) throws GitHubServiceException {
		final Repository repository = dao.get(id);
		log.debug(repository);
		return repository;
	}

	public void deleteRepo(final int id) {
		dao.delete(id);
	}

	public Repository createRepo(final String repoURL, final int accId) throws GitHubServiceException {
		log.info("repoURL: "+repoURL+", accId: " + accId);
		final Repository repository = new Repository();
		final ConnectedAccount account = credDAO.get(accId);

		repository.setAccount(account);
		repository.setCloneURL(repoURL);
		final Matcher matcher = urlPatten.matcher(repoURL);
		if (matcher.find()) {
			final String user = matcher.group(1);
			final String name = matcher.group(2);
			final RepositoryService service = new RepositoryService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				final org.eclipse.egit.github.core.Repository repository2 = service.getRepository(user, name);
				repository.setName(repository2.getName());
			} catch (final IOException e) {
				throw new GitHubServiceException(e);
			}
		}

		dao.persist(repository);
		return repository;
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

	public List<Branch> getBranches(final int id) throws GitHubServiceException {
		final Repository repository = dao.get(id);
		log.info(repository);
		if (repository.getBranches() == null || repository.getBranches().isEmpty()) {
			return updateBranches(id);
		}
		return repository.getBranches();
	}

	public List<Branch> updateBranches(final int id) throws GitHubServiceException {
		final List<Branch> list = new ArrayList<>();
		final Repository repository = dao.get(id);
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
					final Branch br = new Branch();
					br.setName(b.getName());
					br.setUrl(b.getCommit().getType());
					br.setSha(b.getCommit().getSha());
					br.setRepo(repository);
					list.add(br);
				});
			} catch (final IOException e) {
				throw new GitHubServiceException(e);
			}
		}
		repository.setBranches(list);
		dao.persist(repository);
		return list;
	}

	public Branch readBranch(final long id) {
		return dao.getBranch(id);
	}

	public List<Tag> getTags(final int id) throws GitHubServiceException {
		final Repository repository = dao.get(id);
		if (repository.getTags() == null || repository.getTags().isEmpty()) {
			return updateTags(id);
		}
		return repository.getTags();
	}

	public List<Tag> updateTags(final int id) throws GitHubServiceException {
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
				throw new GitHubServiceException(e);
			}
		}
		repository.setTags(list);
		dao.persist(repository);
		return list;
	}

	public List<String> getCommits(final int id) throws GitHubServiceException {
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
				throw new GitHubServiceException(e);
			}
		}
		return list;
	}

	public RepositoryCommit getCommit(final int id, final String sha1) throws GitHubServiceException {
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
				throw new GitHubServiceException(e);
			}
		}
		return null;
	}

	private String getClientId() {
		return configuration.getGithubClientId();
	}

	private String getClientSecret() {
		return configuration.getGithubClientSecret();
	}

	private String tokenURL() {
		return "https://github.com/login/oauth/access_token";
	}

	private String authorizeURL() {
		return "https://github.com/login/oauth/authorize";
	}

	@Override
	public URI getAuthorizeURIForService(final ConnectedAccount account, final URI redirectUri, final String state,
			final Map<String, Object> additional) {
		final String uriTemplate = authorizeURL()
				+ "?client_id={clientId}&redirect_uri={redirect_uri}&state={state}&scope={scope}";
		final URI url = UriBuilder.fromUri(uriTemplate).build(getClientId(), redirectUri, state,
				"user,public_repo,repo,repo_deployment,gist");
		return url;
	}

	@Override
	public AccountToken getAccessToken(final String code, final String state, final URI redirectUri,
			final Map<String, Object> additional) {
		final JerseyClient client = JerseyClientBuilder.createClient();
		final Form entity = new Form();
		entity.param("client_id", getClientId());
		entity.param("client_secret", getClientSecret());
		entity.param("code", code);
		entity.param("redirect_uri", redirectUri.toString());
		entity.param("state", state);
		final AccountToken tokenResponse = client.target(tokenURL()).request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(entity, MediaType.APPLICATION_FORM_URLENCODED_TYPE), AccountToken.class);
		return tokenResponse;
	}

	@Override
	public boolean verify(final ConnectedAccount account) {
		final RepositoryService service = new RepositoryService();
		service.getClient().setOAuth2Token(account.getToken().getAccessToken());
		try {
			service.getRepositories();
		} catch (final IOException e) {
			log.error("Unable to verify github account", e);
			return false;
		}
		return true;
	}

	public boolean isRepo(final int id) throws GitHubServiceException {
		final Repository repository = dao.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			return gitService.isRepo(repoDir);
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
	}

	public void clone(final int id) throws GitHubServiceException {
		final Repository repository = dao.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			gitService.clone(repoDir, repository.getCloneURL(), repository.getAccount().getToken().getAccessToken(),
					new char[0]);
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
	}

	public void checkout(final int id, final String name) throws GitHubServiceException {
		final Repository repository = dao.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			gitService.checkout(repoDir, name);
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
	}

	public void pull(final int id) throws GitHubServiceException {
		final Repository repository = dao.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			gitService.pull(repoDir, repository.getAccount().getToken().getAccessToken(), new char[0]);
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
	}

	public List<GitDiff> diffBranches(final long id, final long compareId) throws GitHubServiceException {
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
			throw new GitHubServiceException(e);
		}
	}

	public TreeNode getDiffTree(final List<GitDiff> diffs) throws GitHubServiceException {
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
}

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

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.PullResult;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.jvnet.hk2.annotations.Service;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ClientService;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.git.GitService;
import nz.co.trineo.git.GitServiceException;
import nz.co.trineo.model.AccountToken;
import nz.co.trineo.model.Branch;
import nz.co.trineo.model.Client;
import nz.co.trineo.model.ConnectedAccount;
import nz.co.trineo.model.GitDiff;
import nz.co.trineo.model.Repository;
import nz.co.trineo.model.RepositoryType;
import nz.co.trineo.model.Tag;
import nz.co.trineo.model.TreeNode;
import nz.co.trineo.repo.RepoDAO;
import nz.co.trineo.repo.RepoService;

@Service
public class GitHubService implements ConnectedService, RepoService {
	private static final Log log = LogFactory.getLog(GitHubService.class);
	private static final Pattern urlPatten = Pattern.compile("github.com/(.*)/(.*)\\.git");

	private final RepoDAO repoDAO;
	private final AccountDAO credDAO;
	private final AppConfiguration configuration;
	private final ClientService clientService;
	private final GitService gitService;

	@Inject
	public GitHubService(final RepoDAO repoDAO, final AppConfiguration configuration, final AccountDAO credDAO,
			final ClientService clientService, final GitService gitService) {
		this.repoDAO = repoDAO;
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

	private String authorizeURL() {
		return "https://github.com/login/oauth/authorize";
	}

	public void checkout(final int id, final String name) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			gitService.checkout(repoDir, name);
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
	}

	public void clone(final int id) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			gitService.clone(repoDir, repository.getCloneURL(), repository.getAccount().getToken().getAccessToken(),
					new char[0]);
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
	}

	public Repository createRepo(final String repoURL, final int accId) throws GitHubServiceException {
		log.info("repoURL: " + repoURL + ", accId: " + accId);
		final Repository repository = new Repository();
		final ConnectedAccount account = credDAO.get(accId);

		repository.setAccount(account);
		repository.setCloneURL(repoURL);
		repository.setType(RepositoryType.github);
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

		repoDAO.persist(repository);
		return repository;
	}

	public void deleteRepo(final int id) {
		repoDAO.delete(id);
	}

	public List<GitDiff> diffBranches(final long id, final long compareId) throws GitHubServiceException {
		final Branch branch = repoDAO.getBranch(id);
		final Branch branch2 = repoDAO.getBranch(compareId);
		final Repository repo = branch.getRepo();
		final File repoDir = new File(configuration.getGithubDirectory(), repo.getName());
		if (!isRepo(repo.getId())) {
			clone(repo.getId());
		}
		pull(repo.getId());
		try {
			return gitService.diff(repoDir, "refs/heads/" + branch.getName(), "refs/heads/" + branch2.getName(), null);
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
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
	public URI getAuthorizeURIForService(final ConnectedAccount account, final URI redirectUri, final String state,
			final Map<String, Object> additional) {
		final String uriTemplate = authorizeURL()
				+ "?client_id={clientId}&redirect_uri={redirect_uri}&state={state}&scope={scope}";
		final URI url = UriBuilder.fromUri(uriTemplate).build(getClientId(), redirectUri, state,
				"user,public_repo,repo,repo_deployment,gist");
		return url;
	}

	public List<Branch> getBranches(final int id) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
		log.info(repository);
		if (repository.getBranches() == null || repository.getBranches().isEmpty()) {
			return updateBranches(id);
		}
		return repository.getBranches();
	}

	public Branch createBranch(final int id, final String branchName) throws GitHubServiceException {
		final Branch branch = repoDAO.getBranch(id);
		final Repository repository = branch.getRepo();
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		final Branch newBranch = new Branch();
		if (matcher.find()) {
			final String user = matcher.group(1);
			final String name = matcher.group(2);
			final DataService service = new DataService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				final TypedResource object = new TypedResource();
				object.setSha(branch.getSha());
				final Reference reference = new Reference();
				reference.setObject(object);
				reference.setRef("refs/heads/" + branchName);
				final Reference createReference = service.createReference(RepositoryId.create(user, name), reference);
				newBranch.setName(branchName);
				newBranch.setOrg(branch.getOrg());
				newBranch.setRepo(repository);
				newBranch.setSha(createReference.getObject().getSha());
				repository.getBranches().add(newBranch);
			} catch (final IOException e) {
				throw new GitHubServiceException(e);
			}
		}
		repoDAO.persist(repository);
		return newBranch;
	}

	private String getClientId() {
		return configuration.getGithubClientId();
	}

	private String getClientSecret() {
		return configuration.getGithubClientSecret();
	}

	public RepositoryCommit getCommit(final int id, final String sha1) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
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

	public List<String> getCommits(final int id) throws GitHubServiceException {
		final List<String> list = new ArrayList<>();
		final Repository repository = repoDAO.get(id);
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

	@Override
	public String getName() {
		return "GitHub";
	}

	public Repository getRepo(final int id) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
		log.debug(repository);
		return repository;
	}

	public List<Repository> getRepos() throws GitHubServiceException {
		return repoDAO.listByType(RepositoryType.github);
	}

	public List<Tag> getTags(final int id) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
		if (repository.getTags() == null || repository.getTags().isEmpty()) {
			return updateTags(id);
		}
		return repository.getTags();
	}

	public boolean isRepo(final int id) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			return gitService.isRepo(repoDir);
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
	}

	public void pull(final int id) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
		final File repoDir = new File(configuration.getGithubDirectory(), repository.getName());
		try {
			final PullResult pullResult = gitService.pull(repoDir, repository.getAccount().getToken().getAccessToken(),
					new char[0]);
			if (!pullResult.isSuccessful()) {
				throw new GitHubServiceException("Error while pulling from remote.");
			}
		} catch (final GitServiceException e) {
			throw new GitHubServiceException(e);
		}
	}

	public Branch readBranch(final long id) {
		return repoDAO.getBranch(id);
	}

	private String tokenURL() {
		return "https://github.com/login/oauth/access_token";
	}

	public List<Branch> updateBranches(final int id) throws GitHubServiceException {
		final Repository repository = repoDAO.get(id);
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
				throw new GitHubServiceException(e);
			}
		}
		repository.setBranches(list);
		repoDAO.persist(repository);
		return list;
	}

	public Repository updateRepo(final Repository repo) {
		final Repository repository = repoDAO.get(repo.getId());
		if (repo.getClient() != null) {
			final Client client = clientService.read(repo.getClient().getId());
			repository.setClient(client);
			if (!client.getRepositories().contains(repository)) {
				client.getRepositories().add(repository);
			}
			clientService.update(client);
		}
		repoDAO.persist(repository);
		return repository;
	}

	public List<Tag> updateTags(final int id) throws GitHubServiceException {
		final List<Tag> list = new ArrayList<>();
		final Repository repository = repoDAO.get(id);
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
		repoDAO.persist(repository);
		return list;
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
}

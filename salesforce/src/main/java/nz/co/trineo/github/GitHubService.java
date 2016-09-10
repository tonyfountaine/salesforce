package nz.co.trineo.github;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

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
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.github.model.Repository;

public class GitHubService implements ConnectedService {
	private static final Log log = LogFactory.getLog(GitHubService.class);
	private static final Pattern urlPatten = Pattern.compile("github.com/(.*)/(.*)\\.git");

	private final GitHubRepoDAO dao;
	private final AccountDAO credDAO;
	private final AppConfiguration configuration;

	public GitHubService(final GitHubRepoDAO dao, final AppConfiguration configuration, final AccountDAO credDAO) {
		this.dao = dao;
		this.configuration = configuration;
		this.credDAO = credDAO;
	}

	@Override
	public String getName() {
		return "GitHub";
	}

	public List<Repository> getRepos() throws GitHubServiceException {
		return dao.listAll();
	}

	public Repository getRepo(final String name) throws GitHubServiceException {
		return dao.getByName(name);
	}

	public Repository createRepo(final String repoURL, final int accId) throws GitHubServiceException {
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
				org.eclipse.egit.github.core.Repository repository2 = service.getRepository(user, name);
				repository.setName(repository2.getName());
			} catch (IOException e) {
				throw new GitHubServiceException(e);
			}
		}

		dao.persist(repository);
		return repository;
	}

	public List<String> getBranches(final String name) throws GitHubServiceException {
		final List<String> list = new ArrayList<>();
		final Repository repository = dao.getByName(name);
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		if (matcher.find()) {
			final String user = matcher.group(1);
			final RepositoryService service = new RepositoryService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				List<RepositoryBranch> branches = service.getBranches(RepositoryId.create(user, name));
				branches.forEach(b -> {
					list.add(b.getName());
				});
			} catch (IOException e) {
				throw new GitHubServiceException(e);
			}
		}
		return list;
	}

	public List<String> getTags(final String name) throws GitHubServiceException {
		final List<String> list = new ArrayList<>();
		final Repository repository = dao.getByName(name);
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		if (matcher.find()) {
			final String user = matcher.group(1);
			final RepositoryService service = new RepositoryService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				List<RepositoryTag> tags = service.getTags(RepositoryId.create(user, name));
				tags.forEach(t -> {
					list.add(t.getName());
				});
			} catch (IOException e) {
				throw new GitHubServiceException(e);
			}
		}
		return list;
	}

	public List<String> getCommits(final String name) throws GitHubServiceException {
		final List<String> list = new ArrayList<>();
		final Repository repository = dao.getByName(name);
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		if (matcher.find()) {
			final String user = matcher.group(1);
			final CommitService service = new CommitService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				List<RepositoryCommit> tags = service.getCommits(RepositoryId.create(user, name));
				tags.forEach(t -> {
					list.add(t.getSha());
				});
			} catch (IOException e) {
				throw new GitHubServiceException(e);
			}
		}
		return list;
	}

	public RepositoryCommit getCommit(final String name, final String sha1) throws GitHubServiceException {
		final Repository repository = dao.getByName(name);
		final ConnectedAccount account = repository.getAccount();
		final Matcher matcher = urlPatten.matcher(repository.getCloneURL());
		if (matcher.find()) {
			final String user = matcher.group(1);
			final CommitService service = new CommitService();
			service.getClient().setOAuth2Token(account.getToken().getAccessToken());
			try {
				return service.getCommit(RepositoryId.create(user, name), sha1);
			} catch (IOException e) {
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
}

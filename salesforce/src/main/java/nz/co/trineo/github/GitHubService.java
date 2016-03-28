package nz.co.trineo.github;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.Repository;
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

public class GitHubService implements ConnectedService {
	private static final Log log = LogFactory.getLog(GitHubService.class);

	private final AccountDAO dao;
	private final AppConfiguration configuration;

	public GitHubService(final AccountDAO dao, final AppConfiguration configuration) {
		this.dao = dao;
		this.configuration = configuration;
	}

	@Override
	public String getName() {
		return "GitHub";
	}

	public List<Repository> getRepos(final int accID) throws Exception {
		final ConnectedAccount account = dao.get(accID);
		final RepositoryService service = new RepositoryService();
		service.getClient().setOAuth2Token(account.getToken().getAccessToken());
		final List<Repository> repositories = service.getRepositories();
		return repositories;
	}

	public List<Repository> getRepos(final String user, final int accID) throws Exception {
		final ConnectedAccount account = dao.get(accID);
		final RepositoryService service = new RepositoryService();
		service.getClient().setOAuth2Token(account.getToken().getAccessToken());
		final List<Repository> repositories = service.getRepositories(user);
		return repositories;
	}

	public Repository getRepo(final String user, final String name, final int accId) throws Exception {
		final ConnectedAccount account = dao.get(accId);
		final RepositoryService service = new RepositoryService();
		service.getClient().setOAuth2Token(account.getToken().getAccessToken());
		return service.getRepository(user, name);
	}

	public List<RepositoryBranch> getBranches(final String user, final String name, final int accId) throws Exception {
		final ConnectedAccount account = dao.get(accId);
		final RepositoryService service = new RepositoryService();
		service.getClient().setOAuth2Token(account.getToken().getAccessToken());
		return service.getBranches(RepositoryId.create(user, name));
	}

	public List<RepositoryTag> getTags(final String user, final String name, final int accId) throws Exception {
		final ConnectedAccount account = dao.get(accId);
		final RepositoryService service = new RepositoryService();
		service.getClient().setOAuth2Token(account.getToken().getAccessToken());
		return service.getTags(RepositoryId.create(user, name));
	}

	public List<RepositoryCommit> getCommits(final String user, final String name, final int accId) throws Exception {
		final ConnectedAccount account = dao.get(accId);
		final CommitService service = new CommitService();
		service.getClient().setOAuth2Token(account.getToken().getAccessToken());
		return service.getCommits(RepositoryId.create(user, name));
	}

	public RepositoryCommit getCommit(final String user, final String name, final int accId, final String sha1)
			throws Exception {
		final ConnectedAccount account = dao.get(accId);
		final CommitService service = new CommitService();
		service.getClient().setOAuth2Token(account.getToken().getAccessToken());
		return service.getCommit(RepositoryId.create(user, name), sha1);
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

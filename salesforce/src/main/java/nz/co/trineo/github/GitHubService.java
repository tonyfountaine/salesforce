package nz.co.trineo.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.TokenRequest;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;
import nz.co.trineo.github.model.Repository;
import nz.co.trineo.github.model.User;

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
		final List<Repository> repos = new ArrayList<>();
		final ConnectedAccount account = dao.get(accID);
		final GitHub git = GitHub.connectUsingOAuth(account.getToken().getAccessToken());
		final Map<String, GHRepository> repoMap = git.getMyself().getAllRepositories();
		for (GHRepository repository : repoMap.values()) {
			final Repository repo = toRepository(repository);
			repos.add(repo);
		}
		return repos;
	}

	public Repository getRepo(final String user, final String name, final int accId) throws Exception {
		final ConnectedAccount account = dao.get(accId);
		final GitHub git = GitHub.connectUsingOAuth(account.getToken().getAccessToken());
		final GHRepository repo = git.getRepository(user + "/" + name);
		return toRepository(repo);
	}

	private Repository toRepository(final GHRepository ghRepository) throws URISyntaxException, IOException {
		final Repository repo = new Repository();
		repo.setDescription(ghRepository.getDescription());
		if (StringUtils.isNotBlank(ghRepository.getHomepage())) {
			repo.setHomepage(URI.create(ghRepository.getHomepage()));
		}
		repo.setId(ghRepository.getId());
		repo.setName(ghRepository.getName());
		repo.setOwner(toUser(ghRepository.getOwner()));
		if (ghRepository.getUrl() != null) {
			repo.setUrl(ghRepository.getUrl().toURI());
		}
		repo.setCloneUrl(ghRepository.gitHttpTransportUrl());
		repo.setDefaultBranch(ghRepository.getDefaultBranch());
		repo.setDownloads(ghRepository.hasDownloads());
		repo.setFork(ghRepository.isFork());
		repo.setForks(ghRepository.getForks());
		repo.setFullName(ghRepository.getFullName());
		repo.setGitUrl(ghRepository.getGitTransportUrl());
		if (ghRepository.getHtmlUrl() != null) {
			repo.setHtmlUrl(ghRepository.getHtmlUrl().toURI());
		}
		repo.setIssues(ghRepository.hasIssues());
		repo.setLanguage(ghRepository.getLanguage());
		repo.setMirrorUrl(ghRepository.getMirrorUrl());
		repo.setNetworkCount(ghRepository.getNetworkCount());
		repo.setOpenIssues(ghRepository.getOpenIssueCount());
		repo.setPrivate(ghRepository.isPrivate());
		repo.setPushedAt(ghRepository.getPushedAt());
		repo.setSize(ghRepository.getSize());
		repo.setSshUrl(ghRepository.getSshUrl());
		repo.setSubscribersCount(ghRepository.getSubscribersCount());
		repo.setSvnUrl(ghRepository.getSvnUrl());
		repo.setWatchers(ghRepository.getWatchers());
		repo.setWiki(ghRepository.hasWiki());
		return repo;
	}

	private User toUser(final GHUser ghUser) throws IOException, URISyntaxException {
		final User user = new User();
		if (StringUtils.isNotBlank(ghUser.getBlog())) {
			user.setBlog(URI.create(ghUser.getBlog()));
		}
		user.setCompany(ghUser.getCompany());
		user.setEmail(ghUser.getEmail());
		user.setId(ghUser.getId());
		user.setLocation(ghUser.getLocation());
		user.setLogin(ghUser.getLogin());
		user.setName(ghUser.getName());
		if (ghUser.getUrl() != null) {
			user.setUrl(ghUser.getUrl().toURI());
		}
		return user;
	}

	public String getClientId() {
		return configuration.getGithubClientId();
	}

	public String getClientSecret() {
		return configuration.getGithubClientSecret();
	}

	public String tokenURL() {
		return "https://github.com/login/oauth/access_token";
	}

	public String authorizeURL() {
		return "https://github.com/login/oauth/authorize";
	}

	@Override
	public URI getAuthorizeURIForService(final ConnectedAccount account, final URI redirectUri, final String state) {
		final String uriTemplate = authorizeURL() + "?client_id={clientId}&redirect_uri={redirect_uri}&state={state}";
		final URI url = UriBuilder.fromUri(uriTemplate).build(getClientId(), redirectUri, state);
		return url;
	}

	@Override
	public AccountToken getAccessToken(final String code, final String state, final URI redirectUri) {
		final JerseyClient client = JerseyClientBuilder.createClient();
		final TokenRequest entity = new TokenRequest();
		entity.client_id = getClientId();
		entity.client_secret = getClientSecret();
		entity.code = code;
		entity.redirect_uri = redirectUri.toString();
		entity.state = state;
		final AccountToken tokenResponse = client.target(tokenURL()).request().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(entity, MediaType.APPLICATION_JSON_TYPE), AccountToken.class);
		return tokenResponse;
	}
}

package nz.co.trineo.common;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.common.hash.HashCode;

import nz.co.trineo.common.model.ConnectedAccount;

public class AccountService {
	private final AccountDAO accountDAO;

	private final Map<String, ConnectedAccount> states = new HashMap<>();

	static class TokenRequest {
		public String client_id;
		public String client_secret;
		public String code;
		public String redirect_uri;
		public String state;
	}

	static class TokenResopnse {
		public String access_token;
		public String scpoe;
		public String token_type;
	}

	public AccountService(AccountDAO accountDAO) {
		super();
		this.accountDAO = accountDAO;
	}

	public List<ConnectedAccount> list() {
		return accountDAO.listAll();
	}

	public ConnectedAccount create(ConnectedAccount account) {
		return accountDAO.persist(account);
	}

	public ConnectedAccount read(int id) {
		return accountDAO.get(id);
	}

	public ConnectedAccount update(ConnectedAccount account) {
		return accountDAO.persist(account);
	}

	public void delete(int id) {
		accountDAO.delete(id);
	}

	public URI getAuthorizeURIForService(final ConnectedAccount account, final URI redirectUri) {
		final String state = HashCode.fromLong(System.currentTimeMillis()).toString();

		states.put(state, account);

		final ConnectedService service = ServiceRegistry.getService(account.getService());
		final String uriTemplate = service.authorizeURL()
				+ "?client_id={clientId}&redirect_uri={redirect_uri}&state={state}";
		final URI url = UriBuilder.fromUri(uriTemplate).build(service.getClientId(), redirectUri, state);
		return url;
	}

	public void getAccessToken(final String code, final String state, final URI redirectUri) {
		final ConnectedAccount account = states.get(state);
		final ConnectedService service = ServiceRegistry.getService(account.getService());

		final JerseyClient client = JerseyClientBuilder.createClient();
		final TokenRequest entity = new TokenRequest();
		entity.client_id = service.getClientId();
		entity.client_secret = service.getClientSecret();
		entity.code = code;
		entity.redirect_uri = redirectUri.toString();
		entity.state = state;
		final TokenResponse tokenResponse = client.target(service.tokenURL()).request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(entity, MediaType.APPLICATION_JSON_TYPE), TokenResponse.class);

		final String accessToken = tokenResponse.getAccessToken();
		final String refreshToken = tokenResponse.getRefreshToken();
		final Long expiresInSeconds = tokenResponse.getExpiresInSeconds();

		account.setAccessToken(accessToken);
		account.setExpiresInSeconds(expiresInSeconds);
		account.setRefreshToken(refreshToken);
		create(account);
	}
}

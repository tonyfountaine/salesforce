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

import com.google.common.hash.HashCode;

import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;

public class AccountService {
	private final AccountDAO accountDAO;

	private final Map<String, ConnectedAccount> states = new HashMap<>();

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
		return service.getAuthorizeURIForService(account, redirectUri, state);
	}

	public void getAccessToken(final String code, final String state, final URI redirectUri) {
		final ConnectedAccount account = states.get(state);
		final ConnectedService service = ServiceRegistry.getService(account.getService());

		final AccountToken tokenResponse = service.getAccessToken(code, state, redirectUri);

		account.setToken(tokenResponse);
		create(account);
	}
}

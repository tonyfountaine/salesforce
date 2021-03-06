package nz.co.trineo.common;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jvnet.hk2.annotations.Service;

import com.google.common.hash.HashCode;

import nz.co.trineo.model.AccountToken;
import nz.co.trineo.model.ConnectedAccount;

@Service
public class AccountService {
	private final AccountDAO accountDAO;

	private static final Map<String, ConnectedAccount> states = new HashMap<>();
	private static final Map<String, Map<String, Object>> additionalMap = new HashMap<>();

	@Inject
	public AccountService(final AccountDAO accountDAO) {
		super();
		this.accountDAO = accountDAO;
	}

	public List<ConnectedAccount> byService(final String service) {
		return accountDAO.listByService(service);
	}

	public ConnectedAccount create(final ConnectedAccount account) {
		return accountDAO.persist(account);
	}

	public void delete(final int id) {
		accountDAO.delete(id);
	}

	public void getAccessToken(final String code, final String state, final URI redirectUri) {
		try {
			final ConnectedAccount account = states.get(state);
			final Map<String, Object> additional = additionalMap.get(state);
			final String name = account.getService();
			final ConnectedService service = (ConnectedService) ServiceRegistry.getService(name);

			final AccountToken tokenResponse = service.getAccessToken(code, state, redirectUri, additional);

			account.setToken(tokenResponse);
			create(account);
		} finally {
			states.remove(state);
			additionalMap.remove(state);
		}
	}

	public URI getAuthorizeURIForService(final ConnectedAccount account, final URI redirectUri,
			final Map<String, Object> additional) {
		final String state = HashCode.fromLong(System.currentTimeMillis()).toString();

		states.put(state, account);
		additionalMap.put(state, additional);

		final ConnectedService service = (ConnectedService) ServiceRegistry.getService(account.getService());
		return service.getAuthorizeURIForService(account, redirectUri, state, additional);
	}

	public List<ConnectedAccount> list() {
		return accountDAO.listAll();
	}

	public ConnectedAccount read(final int id) {
		return accountDAO.get(id);
	}

	public ConnectedAccount update(final ConnectedAccount account) {
		return accountDAO.persist(account);
	}

	public boolean verify(final int id) {
		final ConnectedAccount account = accountDAO.get(id);
		final ConnectedService service = (ConnectedService) ServiceRegistry.getService(account.getService());
		return service.verify(account);
	}
}

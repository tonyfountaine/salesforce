package nz.co.trineo.common;

import java.net.URI;
import java.util.Map;

import nz.co.trineo.model.AccountToken;
import nz.co.trineo.model.ConnectedAccount;

public interface ConnectedService extends Service {
	AccountToken getAccessToken(String code, String state, URI redirectUri, Map<String, Object> additional);

	URI getAuthorizeURIForService(ConnectedAccount account, URI redirectUri, String state,
			Map<String, Object> additional);

	boolean verify(ConnectedAccount account);
}

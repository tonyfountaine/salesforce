package nz.co.trineo.common;

import java.net.URI;

import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;

public interface ConnectedService {
	String getName();

	URI getAuthorizeURIForService(ConnectedAccount account, URI redirectUri, String state);

	AccountToken getAccessToken(String code, String state, URI redirectUri);
}

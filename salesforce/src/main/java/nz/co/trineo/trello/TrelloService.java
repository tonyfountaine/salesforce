package nz.co.trineo.trello;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TrelloApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Argument;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Member;
import com.julienvey.trello.impl.TrelloImpl;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;

public class TrelloService implements ConnectedService {
	private final AppConfiguration configuration;
	private final AccountDAO credDAO;

	public TrelloService(final AppConfiguration configuration, final AccountDAO credDAO) {
		this.configuration = configuration;
		this.credDAO = credDAO;
	}

	@Override
	public String getName() {
		return "Trello";
	}

	public Member getMe(final int accId) {
		final Trello trello = getTrello(accId);
		return trello.getMemberInformation("me");
	}

	public List<String> listBoards(final int accId) {
		final Trello trello = getTrello(accId);
		final Member member = trello.getMemberInformation("me");
		return member.getIdBoards();
	}

	public Board getBoard(final String id, final int accId) {
		final Trello trello = getTrello(accId);
		return trello.getBoard(id);
	}

	private Trello getTrello(final int accId) {
		final ConnectedAccount account = credDAO.get(accId);
		final Trello trello = new TrelloImpl(configuration.getTrelloKey(), account.getToken().getAccessToken());
		return trello;
	}

	public List<Card> getCards(final String id, final int accId) {
		final Trello trello = getTrello(accId);
		return trello.getBoardCards(id, new Argument("fields", "name,idList,url"));
	}

	public Card getCard(final String id, final int accId) {
		final Trello trello = getTrello(accId);
		return trello.getCard(id);
	}

	public String getClientId() {
		return configuration.getTrelloKey();
	}

	public String getClientSecret() {
		return configuration.getTrelloSecret();
	}

	public String authorizeURL() {
		return "https://trello.com/1/OAuthAuthorizeToken";
	}

	public String tokenURL() {
		return "https://trello.com/1/OAuthGetAccessToken";
	}

	private final Map<String, Token> tokenMap = new HashMap<>();

	@Override
	public URI getAuthorizeURIForService(final ConnectedAccount account, final URI redirectUri, final String state,
			final Map<String, Object> additional) {
		final OAuthService service = getOAuthService(redirectUri, state);
		final Token requestToken = service.getRequestToken();
		tokenMap.put(state, requestToken);
		final String authorizationUrl = service.getAuthorizationUrl(requestToken);
		return URI.create(authorizationUrl);
	}

	private OAuthService getOAuthService(final URI redirectUri, final String state) {
		return new ServiceBuilder().provider(TrelloApi.class).apiKey(configuration.getTrelloKey())
				.apiSecret(configuration.getTrelloSecret()).debug().callback(redirectUri.toString() + "/" + state)
				.build();
	}

	@Override
	public AccountToken getAccessToken(final String code, final String state, final URI redirectUri,
			final Map<String, Object> additional) {
		final OAuthService service = getOAuthService(redirectUri, state);

		// getting access token
		final Token requestToken = tokenMap.get(state);
		final Verifier verifier = new Verifier(code);
		final Token accessToken = service.getAccessToken(requestToken, verifier);

		final AccountToken tokenResponse = new AccountToken();
		tokenResponse.setAccessToken(accessToken.getToken());
		return tokenResponse;
	}
}

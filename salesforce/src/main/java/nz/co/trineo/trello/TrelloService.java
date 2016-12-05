package nz.co.trineo.trello;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.model.AccountToken;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;

public class TrelloService implements ConnectedService {
	private final AppConfiguration configuration;
	private final AccountDAO credDAO;
	private final BoardDAO boardDAO;

	public TrelloService(final AppConfiguration configuration, final AccountDAO credDAO, final BoardDAO boardDAO) {
		this.configuration = configuration;
		this.credDAO = credDAO;
		this.boardDAO = boardDAO;
	}

	@Override
	public String getName() {
		return "Trello";
	}

	public Member getMe(final int accId) {
		final ConnectedAccount account = credDAO.get(accId);
		final Trello trello = getTrello(account);
		return trello.getMemberInformation("me");
	}

	public List<nz.co.trineo.trello.model.Board> listBoards() {
		return boardDAO.listAll();
	}

	public List<nz.co.trineo.trello.model.Board> listBoards(final int accId) {
		final ConnectedAccount account = credDAO.get(accId);
		final Trello trello = getTrello(account);
		final Member member = trello.getMemberInformation("me");
		final List<nz.co.trineo.trello.model.Board> boards = new ArrayList<>();
		member.getIdBoards().forEach(b -> {
			final Board tBoard = getBoard(b, accId);
			final nz.co.trineo.trello.model.Board board = new nz.co.trineo.trello.model.Board();
			board.setAccount(account);
			board.setClosed(tBoard.isClosed());
			board.setDateLastActivity(tBoard.getDateLastActivity());
			board.setDateLastView(tBoard.getDateLastView());
			board.setDesc(tBoard.getDesc());
			board.setId(tBoard.getId());
			board.setIdOrganization(tBoard.getIdOrganization());
			// board.setLabelNames(tBoard.getLabelNames());
			board.setName(tBoard.getName());
			board.setShortLink(tBoard.getShortLink());
			board.setShortUrl(tBoard.getShortUrl());
			board.setSubscribed(tBoard.isSubscribed());
			board.setUrl(tBoard.getUrl());
			boardDAO.persist(board);
			boards.add(board);
		});
		return boards;
	}

	public nz.co.trineo.trello.model.Board getBoard(final String id) {
		return boardDAO.get(id);
	}

	public Board getBoard(final String id, final int accId) {
		final ConnectedAccount account = credDAO.get(accId);
		final Trello trello = getTrello(account);
		return trello.getBoard(id);
	}

	private Trello getTrello(final ConnectedAccount account) {
		final Trello trello = new TrelloImpl(configuration.getTrelloKey(), account.getToken().getAccessToken());
		return trello;
	}

	public List<Card> getCards(final String id) {
		return getCards(id, null);
	}

	public List<Card> getCards(final String id, final String listId) {
		nz.co.trineo.trello.model.Board board = boardDAO.get(id);
		final Trello trello = getTrello(board.getAccount());
		List<Card> boardCards = trello.getBoardCards(id,
				new Argument("fields", "name,idList,url,desc,labels,badges,shortLink,shortUrl"));
		if (listId != null) {
			boardCards = boardCards.stream().filter(c -> listId.equals(c.getIdList())).collect(Collectors.toList());
		}
		return boardCards;
	}

	public List<TList> getLists(final String id) {
		nz.co.trineo.trello.model.Board board = boardDAO.get(id);
		final Trello trello = getTrello(board.getAccount());
		return trello.getBoardLists(id);
	}

	public Card getCard(final String id, final int accId) {
		final ConnectedAccount account = credDAO.get(accId);
		final Trello trello = getTrello(account);
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

	@Override
	public boolean verify(final ConnectedAccount account) {
		// TODO Auto-generated method stub
		return false;
	}
}

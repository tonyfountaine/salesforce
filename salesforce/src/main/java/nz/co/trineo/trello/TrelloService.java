package nz.co.trineo.trello;

import java.util.List;

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Argument;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Member;
import com.julienvey.trello.impl.TrelloImpl;

import nz.co.trineo.common.AccountDAO;
import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.configuration.AppConfiguration;

public class TrelloService implements ConnectedService {
	private final AppConfiguration configuration;
	private final AccountDAO credDAO;

	public TrelloService(AppConfiguration configuration, final AccountDAO credDAO) {
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
		Member member = trello.getMemberInformation("me");
		return member.getIdBoards();
	}

	public Board getBoard(final String id, final int accId) {
		final Trello trello = getTrello(accId);
		return trello.getBoard(id);
	}

	private Trello getTrello(final int accId) {
		final ConnectedAccount account = credDAO.get(accId);
		final Trello trello = new TrelloImpl(configuration.getTrelloKey(), account.getCredentals().getAuthKey());
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

	@Override
	public boolean usesOAuth() {
		return true;
	}

	@Override
	public String getClientId() {
		return configuration.getTrelloKey();
	}

	@Override
	public String getClientSecret() {
		return configuration.getTrelloSecret();
	}
}

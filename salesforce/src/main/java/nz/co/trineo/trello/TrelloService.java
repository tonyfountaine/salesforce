package nz.co.trineo.trello;

import java.util.List;

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Argument;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Member;
import com.julienvey.trello.impl.TrelloImpl;

import nz.co.trineo.common.ConnectedService;
import nz.co.trineo.configuration.AppConfiguration;

public class TrelloService implements ConnectedService {
	private final AppConfiguration configuration;

	public TrelloService(AppConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getName() {
		return "Trello";
	}
	
	public Member getMe() {
		final Trello trello = getTrello();
		return trello.getMemberInformation("me");
	}

	public List<String> listBoards() {
		final Trello trello = getTrello();
		Member member = trello.getMemberInformation("tonyfountaine");
		return member.getIdBoards();
	}

	public Board getBoard(final String id) {
		final Trello trello = getTrello();
		return trello.getBoard(id);
	}

	private Trello getTrello() {
		final Trello trello = new TrelloImpl(configuration.getTrelloKey(), configuration.getTrelloToken());
		return trello;
	}

	public List<Card> getCards(final String id) {
		final Trello trello = getTrello();
		return trello.getBoardCards(id, new Argument("fields","name,idList,url"));
	}

	public Card getCard(final String id) {
		final Trello trello = getTrello();
		return trello.getCard(id);
	}
}

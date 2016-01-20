package nz.co.trineo.trello;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Member;

@Path("/trello")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrelloResource {
	private final TrelloService service;

	public TrelloResource(TrelloService service) {
		this.service = service;
	}

	@GET
	@Timed
	@Path("/me")
	public Response getMe() {
		final Member member = service.getMe();
		return Response.ok(member).build();
	}

	@GET
	@Timed
	@Path("/boards")
	public Response listBoards() {
		final List<String> list = service.listBoards();
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/boards/{id}")
	public Response getBoard(final @PathParam("id") String id) {
		final Board board = service.getBoard(id);
		return Response.ok(board).build();
	}

	@GET
	@Timed
	@Path("/boards/{id}/cards")
	public Response getCards(final @PathParam("id") String id) {
		final List<Card> cards = service.getCards(id);
		return Response.ok(cards).build();
	}

	@GET
	@Timed
	@Path("/cards/{id}")
	public Response getCard(final @PathParam("id") String id) {
		final Card card = service.getCard(id);
		return Response.ok(card).build();
	}
}

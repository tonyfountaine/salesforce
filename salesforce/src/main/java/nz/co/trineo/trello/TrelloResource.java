package nz.co.trineo.trello;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Member;

import io.dropwizard.hibernate.UnitOfWork;

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
	@UnitOfWork
	public Response getMe(final @QueryParam("acc") int accId) {
		final Member member = service.getMe(accId);
		return Response.ok(member).build();
	}

	@GET
	@Timed
	@Path("/boards")
	@UnitOfWork
	public Response listBoards(final @QueryParam("acc") int accId) {
		final List<String> list = service.listBoards(accId);
		return Response.ok(list).build();
	}

	@GET
	@Timed
	@Path("/boards/{id}")
	@UnitOfWork
	public Response getBoard(final @PathParam("id") String id,final @QueryParam("acc") int accId) {
		final Board board = service.getBoard(id,accId);
		return Response.ok(board).build();
	}

	@GET
	@Timed
	@Path("/boards/{id}/cards")
	@UnitOfWork
	public Response getCards(final @PathParam("id") String id,final @QueryParam("acc") int accId) {
		final List<Card> cards = service.getCards(id,accId);
		return Response.ok(cards).build();
	}

	@GET
	@Timed
	@Path("/cards/{id}")
	@UnitOfWork
	public Response getCard(final @PathParam("id") String id,final @QueryParam("acc") int accId) {
		final Card card = service.getCard(id,accId);
		return Response.ok(card).build();
	}
}

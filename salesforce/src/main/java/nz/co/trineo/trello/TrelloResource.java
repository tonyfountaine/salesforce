package nz.co.trineo.trello;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.julienvey.trello.domain.Action;
import com.julienvey.trello.domain.Attachment;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Member;
import com.julienvey.trello.domain.TList;

import io.dropwizard.hibernate.UnitOfWork;
import nz.co.trineo.model.Board;
import nz.co.trineo.salesforce.SalesforceException;

@Path("/trello")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrelloResource {
	private final TrelloService service;

	@Inject
	public TrelloResource(final TrelloService service) {
		this.service = service;
	}

	@GET
	@Timed
	@Path("/boards/{id}")
	@UnitOfWork
	public Response getBoard(final @PathParam("id") String id) {
		final Board board = service.getBoard(id);
		return Response.ok(board).build();
	}

	@GET
	@Timed
	@Path("/boards/{boardId}/cards/{cardId}")
	@UnitOfWork
	public Response getCard(final @PathParam("boardId") String id, final @PathParam("cardId") String cardId) {
		final Card card = service.getCard(id, cardId);
		return Response.ok(card).build();
	}

	@GET
	@Timed
	@Path("/boards/{boardId}/cards/{cardId}/comments")
	@UnitOfWork
	public Response getCardComments(final @PathParam("boardId") String id, final @PathParam("cardId") String cardId) {
		final List<Action> cardComments = service.getCardComments(id, cardId);
		return Response.ok(cardComments).build();
	}

	@GET
	@Timed
	@Path("/boards/{boardId}/cards/{cardId}/attachments")
	@UnitOfWork
	public Response getCardAttachments(final @PathParam("boardId") String id,
			final @PathParam("cardId") String cardId) {
		final List<Attachment> cardComments = service.getCardAttachments(id, cardId);
		return Response.ok(cardComments).build();
	}

	@GET
	@Timed
	@Path("/boards/{id}/cards")
	@UnitOfWork
	public Response getCards(final @PathParam("id") String id, final @QueryParam("list") String list) {
		final List<Card> cards = service.getCards(id, list);
		return Response.ok(cards).build();
	}

	@GET
	@Timed
	@Path("/boards/{id}/lists")
	@UnitOfWork
	public Response getListss(final @PathParam("id") String id) {
		final List<TList> lists = service.getLists(id);
		return Response.ok(lists).build();
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
	public Response listBoards() {
		final List<Board> list = service.listBoards();
		return Response.ok(list).build();
	}

	@POST
	@Timed
	@Path("/boards")
	@UnitOfWork
	public Response listBoards(final @QueryParam("acc") int accId) {
		final List<Board> list = service.listBoards(accId);
		return Response.ok(list).build();
	}

	@PUT
	@Path("/boards")
	@Timed
	@UnitOfWork
	public Response updateBoard(final Board board) throws SalesforceException {
		final Board updatedBoard = service.updateBoard(board);
		return Response.ok(updatedBoard).build();
	}
}

package nz.co.trineo.common;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

@Path("/")
public class StaticResource {

	@GET
	@Path("{name}")
	@Produces(MediaType.TEXT_HTML)
	public Response getHTML(final @PathParam("name") String name) {
		final InputStream in = getClass().getResourceAsStream("/" + name);
		final StreamingOutput stream = output -> {
			try {
				IOUtils.copy(in, output);
			} catch (final Exception e) {
				throw new WebApplicationException(e);
			}
		};

		return Response.ok(stream).build();
	}
}

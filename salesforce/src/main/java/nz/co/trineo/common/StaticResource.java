package nz.co.trineo.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	@Path("js/{name}")
	@Produces({ "application/javascript", "text/javascript" })
	public Response getJS(final @PathParam("name") String name) {
		final InputStream in = getClass().getResourceAsStream("/js/" + name);
		final StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream output) throws IOException, WebApplicationException {
				try {
					IOUtils.copy(in, output);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + name).build();
	}

	@GET
	@Path("css/{name}")
	@Produces("text/css")
	public Response getCSS(final @PathParam("name") String name) {
		final InputStream in = getClass().getResourceAsStream("/css/" + name);
		final StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream output) throws IOException, WebApplicationException {
				try {
					IOUtils.copy(in, output);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + name).build();
	}

	@GET
	@Path("{name}")
	@Produces(MediaType.TEXT_HTML)
	public Response getHTML(final @PathParam("name") String name) {
		final InputStream in = getClass().getResourceAsStream("/" + name);
		final StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream output) throws IOException, WebApplicationException {
				try {
					IOUtils.copy(in, output);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + name).build();
	}
}

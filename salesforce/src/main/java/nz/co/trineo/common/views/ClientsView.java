package nz.co.trineo.common.views;

import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.Client;

public class ClientsView extends View {

	private final List<Client> clients;

	public ClientsView(final List<Client> clients) {
		super("/clients.ftl");
		this.clients = clients;
	}

	public List<Client> getClients() {
		return clients;
	}
}

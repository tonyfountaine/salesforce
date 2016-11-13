package nz.co.trineo.common;

import java.util.List;

import nz.co.trineo.common.model.Client;

public class ClientService {
	private final ClientDAO clientDAO;

	public ClientService(final ClientDAO clientDAO) {
		super();
		this.clientDAO = clientDAO;
	}

	public List<Client> list() {
		return clientDAO.listAll();
	}

	public Client create(final Client client) {
		return clientDAO.persist(client);
	}

	public Client read(final long id) {
		return clientDAO.get(id);
	}

	public Client update(final Client client) {
		return clientDAO.persist(client);
	}

	public void delete(final long id) {
		clientDAO.delete(id);
	}
}

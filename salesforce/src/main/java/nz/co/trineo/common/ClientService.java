package nz.co.trineo.common;

import java.util.List;

import javax.inject.Inject;

import org.jvnet.hk2.annotations.Service;

import nz.co.trineo.model.Client;

@Service
public class ClientService {
	private final ClientDAO clientDAO;

	@Inject
	public ClientService(final ClientDAO clientDAO) {
		super();
		this.clientDAO = clientDAO;
	}

	public Client create(final Client client) {
		return clientDAO.persist(client);
	}

	public void delete(final long id) {
		clientDAO.delete(id);
	}

	public List<Client> list() {
		return clientDAO.listAll();
	}

	public Client read(final long id) {
		return clientDAO.get(id);
	}

	public Client update(final Client client) {
		return clientDAO.persist(client);
	}
}

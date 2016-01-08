package nz.co.trineo.common;

import java.util.List;

import nz.co.trineo.common.model.ConnectedAccount;

public class AccountService {
	private final AccountDAO accountDAO;

	public AccountService(AccountDAO accountDAO) {
		super();
		this.accountDAO = accountDAO;
	}

	public List<ConnectedAccount> list() {
		return accountDAO.listAll();
	}

	public ConnectedAccount create(ConnectedAccount account) {
		return accountDAO.persist(account);
	}

	public ConnectedAccount read(int id) {
		return accountDAO.get(id);
	}

	public ConnectedAccount update(ConnectedAccount account) {
		return accountDAO.persist(account);
	}

	public void delete(int id) {
		accountDAO.delete(id);
	}
}

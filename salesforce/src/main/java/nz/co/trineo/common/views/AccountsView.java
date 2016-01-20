package nz.co.trineo.common.views;

import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.ConnectedAccount;

public class AccountsView extends View {

	private final List<ConnectedAccount> accounts;

	public AccountsView(List<ConnectedAccount> accounts) {
		super("/accounts.ftl");
		this.accounts = accounts;
	}

	public List<ConnectedAccount> getAccounts() {
		return accounts;
	}
}

package nz.co.trineo.common.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.ConnectedAccount;

public class AccountsView extends View {

	private final List<ConnectedAccount> accounts;
	private final List<String> services;

	public AccountsView(List<ConnectedAccount> accounts, final Collection<String> services) {
		super("/accounts.ftl");
		this.accounts = accounts;
		this.services = new ArrayList<>(services);
	}

	public List<ConnectedAccount> getAccounts() {
		return accounts;
	}

	public List<String> getServices() {
		return services;
	}
}

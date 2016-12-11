package nz.co.trineo.common.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.salesforce.model.Environment;

public class AccountsView extends View {

	private final List<ConnectedAccount> accounts;
	private final List<String> services;
	private final EnumSet<Environment> environments = EnumSet.allOf(Environment.class);

	public AccountsView(final List<ConnectedAccount> accounts, final Collection<String> services) {
		super("/accounts.ftl");
		this.accounts = accounts;
		this.services = new ArrayList<>(services);
	}

	public List<ConnectedAccount> getAccounts() {
		return accounts;
	}

	public EnumSet<Environment> getEnvironments() {
		return environments;
	}

	public List<String> getServices() {
		return services;
	}
}

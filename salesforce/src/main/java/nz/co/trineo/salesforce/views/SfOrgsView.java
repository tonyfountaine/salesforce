package nz.co.trineo.salesforce.views;

import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.salesforce.model.Organization;

public class SfOrgsView extends View {
	private final List<Organization> orgs;
	private final List<ConnectedAccount> accounts;

	public SfOrgsView(final List<Organization> orgs, final List<ConnectedAccount> accounts) {
		super("/sforgs.ftl");
		this.orgs = orgs;
		this.accounts = accounts;
	}

	public List<ConnectedAccount> getAccounts() {
		return accounts;
	}

	public List<Organization> getOrgs() {
		return orgs;
	}
}

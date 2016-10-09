package nz.co.trineo.common.views;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.ConnectedAccount;

public class AccountView extends View {
	private final ConnectedAccount account;

	public AccountView(final ConnectedAccount account) {
		super("/account.ftl");
		this.account = account;
	}

	public ConnectedAccount getAccount() {
		return account;
	}

	public String title() {
		return "Account - " + account.getId();
	}
}
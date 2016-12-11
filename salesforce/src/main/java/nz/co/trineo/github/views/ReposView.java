package nz.co.trineo.github.views;

import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.github.model.Repository;

public class ReposView extends View {
	private final List<Repository> repos;
	private final List<ConnectedAccount> accounts;

	public ReposView(final List<Repository> repos, final List<ConnectedAccount> accounts) {
		super("/repos.ftl");
		this.repos = repos;
		this.accounts = accounts;
	}

	public List<ConnectedAccount> getAccounts() {
		return accounts;
	}

	public List<Repository> getRepos() {
		return repos;
	}
}

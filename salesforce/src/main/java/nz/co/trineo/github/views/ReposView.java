package nz.co.trineo.github.views;

import java.util.List;

import org.eclipse.egit.github.core.Repository;

import io.dropwizard.views.View;

public class ReposView extends View {
	private final List<Repository> repos;

	public ReposView(final List<Repository> repos) {
		super("/repos.ftl");
		this.repos = repos;
	}

	public List<Repository> getRepos() {
		return repos;
	}

}

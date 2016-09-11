package nz.co.trineo.github.views;

import io.dropwizard.views.View;
import nz.co.trineo.github.model.Repository;

public class RepoView extends View {
	private final Repository repo;

	public RepoView(final Repository repo) {
		super("/repo.ftl");
		this.repo = repo;
	}

	public Repository getRepo() {
		return repo;
	}
}

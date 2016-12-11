package nz.co.trineo.github.views;

import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.Client;
import nz.co.trineo.github.model.Branch;
import nz.co.trineo.github.model.Repository;
import nz.co.trineo.github.model.Tag;

public class RepoView extends View {
	private final Repository repo;
	private final List<Branch> branches;
	private final List<Tag> tags;
	private final List<Client> clients;

	public RepoView(final Repository repo, final List<Branch> branches, final List<Tag> tags,
			final List<Client> clients) {
		super("/repo.ftl");
		this.repo = repo;
		this.branches = branches;
		this.tags = tags;
		this.clients = clients;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public List<Client> getClients() {
		return clients;
	}

	public Repository getRepo() {
		return repo;
	}

	public List<Tag> getTags() {
		return tags;
	}
}

package nz.co.trineo.salesforce.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.model.Branch;

public class BranchView {
	private long id;
	private String sha;
	private String name;
	private String url;
	private RepositoryView repo;

	public BranchView(final Branch branch) {
		id = branch.getId();
		sha = branch.getSha();
		name = branch.getName();
		url = branch.getUrl();
		repo = new RepositoryView(branch.getRepo());
	}

	@JsonProperty
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@JsonProperty
	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty
	public RepositoryView getRepo() {
		return repo;
	}

	public void setRepo(RepositoryView repo) {
		this.repo = repo;
	}
}
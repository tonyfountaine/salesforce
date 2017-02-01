package nz.co.trineo.repo.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.repo.model.Branch;

public class BranchView {
	private long id;
	private String sha;
	private String name;
	private String url;
	private OrganizationView org;

	public BranchView(final Branch branch) {
		id = branch.getId();
		sha = branch.getSha();
		name = branch.getName();
		url = branch.getUrl();
		if (branch.getOrg() != null) {
			org = new OrganizationView(branch.getOrg());
		}
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
	public OrganizationView getOrg() {
		return org;
	}

	public void setOrg(OrganizationView org) {
		this.org = org;
	}
}
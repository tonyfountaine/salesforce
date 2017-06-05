package nz.co.trineo.salesforce.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.model.Repository;

public class RepositoryView {
	private int id;
	private String name;
	private String cloneURL;

	public RepositoryView(final Repository repository) {
		id = repository.getId();
		name = repository.getName();
		cloneURL = repository.getCloneURL();
	}

	@JsonProperty
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty
	public String getCloneURL() {
		return cloneURL;
	}

	public void setCloneURL(String cloneURL) {
		this.cloneURL = cloneURL;
	}
}
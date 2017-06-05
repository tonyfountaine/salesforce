package nz.co.trineo.salesforce.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.model.Client;

public class ClientView {
	private long id;
	private String name;

	public ClientView(final Client client) {
		id = client.getId();
		name = client.getName();
	}

	@JsonProperty
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
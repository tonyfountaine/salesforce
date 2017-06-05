package nz.co.trineo.salesforce.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.model.ConnectedAccount;

public class AccountView {
	private int id;
	private String service;
	private String name;

	public AccountView(final ConnectedAccount account) {
		id = account.getId();
		service = account.getService();
		name = account.getName();
	}

	@JsonProperty
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonProperty
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
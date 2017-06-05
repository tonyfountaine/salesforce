package nz.co.trineo.repo.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.model.Organization;

public class OrganizationView {
	private String id;
	private String name;
	private String organizationType;
	private boolean sandbox;
	private String nickName;

	public OrganizationView(final Organization organization) {
		id = organization.getId();
		name = organization.getName();
		organizationType = organization.getOrganizationType();
		sandbox = organization.isSandbox();
		nickName = organization.getNickName();
	}

	@JsonProperty
	public String getId() {
		return id;
	}

	public void setId(String id) {
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
	public String getOrganizationType() {
		return organizationType;
	}

	public void setOrganizationType(String organizationType) {
		this.organizationType = organizationType;
	}

	@JsonProperty
	public boolean isSandbox() {
		return sandbox;
	}

	public void setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
	}

	@JsonProperty
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
}
package nz.co.trineo.salesforce.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.model.Organization;

public class OrganizationView {
	private String id;
	private String name;
	private String organizationType;
	private boolean sandbox;
	private AccountView account;
	private String nickName;
	private BranchView branch;
	private ClientView client;

	public OrganizationView(final Organization organization) {
		id = organization.getId();
		name = organization.getName();
		organizationType = organization.getOrganizationType();
		sandbox = organization.isSandbox();
		if (organization.getAccount() != null) {
			account = new AccountView(organization.getAccount());
		}
		nickName = organization.getNickName();
		if (organization.getBranch() != null) {
			branch = new BranchView(organization.getBranch());
		}
		if (organization.getClient() != null) {
			client = new ClientView(organization.getClient());
		}
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
	public AccountView getAccount() {
		return account;
	}

	public void setAccount(AccountView account) {
		this.account = account;
	}

	@JsonProperty
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@JsonProperty
	public BranchView getBranch() {
		return branch;
	}

	public void setBranch(BranchView branch) {
		this.branch = branch;
	}

	@JsonProperty
	public ClientView getClient() {
		return client;
	}

	public void setClient(ClientView client) {
		this.client = client;
	}
}
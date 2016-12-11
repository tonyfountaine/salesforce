package nz.co.trineo.salesforce.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.model.ConnectedAccount;
import nz.co.trineo.github.model.Branch;

@Entity
@Table(name = "sforg")
public class Organization {
	@Id
	private String id;
	@Column
	private String name;
	@Column
	private String organizationType;
	@Column
	private boolean sandbox;
	@OneToOne(cascade = CascadeType.ALL)
	private ConnectedAccount account;
	@OneToMany(cascade = CascadeType.ALL)
	private List<RunTestsResult> testResults = new ArrayList<>();
	@Column
	private String nickName;
	@OneToMany(cascade = CascadeType.ALL)
	private List<Backup> backups = new ArrayList<>();
	@OneToOne
	private Branch branch;
	@ManyToOne
	@JoinColumn(name = "CLIENT_ID")
	private Client client;

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Organization other = (Organization) obj;
		if (account == null) {
			if (other.account != null) {
				return false;
			}
		} else if (!account.equals(other.account)) {
			return false;
		}
		if (backups == null) {
			if (other.backups != null) {
				return false;
			}
		} else if (!backups.equals(other.backups)) {
			return false;
		}
		if (client == null) {
			if (other.client != null) {
				return false;
			}
		} else if (!client.equals(other.client)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (nickName == null) {
			if (other.nickName != null) {
				return false;
			}
		} else if (!nickName.equals(other.nickName)) {
			return false;
		}
		if (organizationType == null) {
			if (other.organizationType != null) {
				return false;
			}
		} else if (!organizationType.equals(other.organizationType)) {
			return false;
		}
		if (sandbox != other.sandbox) {
			return false;
		}
		if (testResults == null) {
			if (other.testResults != null) {
				return false;
			}
		} else if (!testResults.equals(other.testResults)) {
			return false;
		}
		return true;
	}

	@JsonProperty
	public ConnectedAccount getAccount() {
		return account;
	}

	@JsonIgnore
	public List<Backup> getBackups() {
		return backups;
	}

	@JsonIgnore
	public Branch getBranch() {
		return branch;
	}

	@JsonProperty
	public Client getClient() {
		return client;
	}

	@JsonProperty
	public String getId() {
		return id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public String getNickName() {
		return nickName;
	}

	@JsonProperty
	public String getOrganizationType() {
		return organizationType;
	}

	@JsonIgnore
	public List<RunTestsResult> getTestResults() {
		return testResults;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (account == null ? 0 : account.hashCode());
		result = prime * result + (backups == null ? 0 : backups.hashCode());
		result = prime * result + (client == null ? 0 : client.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (nickName == null ? 0 : nickName.hashCode());
		result = prime * result + (organizationType == null ? 0 : organizationType.hashCode());
		result = prime * result + (sandbox ? 1231 : 1237);
		result = prime * result + (testResults == null ? 0 : testResults.hashCode());
		return result;
	}

	@JsonProperty
	public boolean isSandbox() {
		return sandbox;
	}

	public void setAccount(final ConnectedAccount account) {
		this.account = account;
	}

	public void setBackups(final List<Backup> backups) {
		this.backups = backups;
	}

	public void setBranch(final Branch branch) {
		this.branch = branch;
	}

	public void setClient(final Client client) {
		this.client = client;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNickName(final String nickName) {
		this.nickName = nickName;
	}

	public void setOrganizationType(final String organizationType) {
		this.organizationType = organizationType;
	}

	public void setSandbox(final boolean sandbox) {
		this.sandbox = sandbox;
	}

	public void setTestResults(final List<RunTestsResult> testResults) {
		this.testResults = testResults;
	}

	public void update(final Organization that) {
		if (that.nickName != null) {
			nickName = that.nickName;
		}
		if (that.client != null) {
			client = that.client;
		}
	}
}

package nz.co.trineo.salesforce.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.common.model.ConnectedAccount;

@Entity
@Table(name = "sforg")
@JsonInclude(Include.NON_DEFAULT)
public class Organization {
	@Id
	private String id;
	@Column
	private String name;
	@Column
	private String organizationType;
	@Column
	private boolean sandbox;
	@OneToOne
	private ConnectedAccount account;
	@OneToMany(cascade = CascadeType.ALL)
	private List<RunTestsResult> testResults = new ArrayList<>();

	@JsonProperty
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@JsonProperty
	public String getOrganizationType() {
		return organizationType;
	}

	public void setOrganizationType(final String organizationType) {
		this.organizationType = organizationType;
	}

	@JsonProperty
	public boolean isSandbox() {
		return sandbox;
	}

	public void setSandbox(final boolean sandbox) {
		this.sandbox = sandbox;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (account == null ? 0 : account.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (organizationType == null ? 0 : organizationType.hashCode());
		result = prime * result + (sandbox ? 1231 : 1237);
		return result;
	}

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
		return true;
	}

	public ConnectedAccount getAccount() {
		return account;
	}

	public void setAccount(final ConnectedAccount account) {
		this.account = account;
	}

	public List<RunTestsResult> getTestResults() {
		return testResults;
	}

	public void setTestResults(final List<RunTestsResult> testResults) {
		this.testResults = testResults;
	}
}

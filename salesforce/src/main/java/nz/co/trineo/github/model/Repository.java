package nz.co.trineo.github.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.model.ConnectedAccount;

@Entity
@Table(name = "repo")
@JsonInclude(Include.NON_DEFAULT)
public class Repository {
	@Id
	@GeneratedValue
	private int id;
	@Column
	private String name;
	@Column
	private String cloneURL;
	@OneToOne
	private ConnectedAccount account;
	@OneToMany
	private List<Branch> branches;
	@OneToMany
	private List<Tag> tags;
	@ManyToOne
	@JoinColumn(name = "CLIENT_ID")
	private Client client;

	@JsonProperty
	public int getId() {
		return id;
	}

	public void setId(final int id) {
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
	public String getCloneURL() {
		return cloneURL;
	}

	public void setCloneURL(final String cloneURL) {
		this.cloneURL = cloneURL;
	}

	@JsonProperty
	public ConnectedAccount getAccount() {
		return account;
	}

	public void setAccount(final ConnectedAccount account) {
		this.account = account;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (account == null ? 0 : account.hashCode());
		result = prime * result + (branches == null ? 0 : branches.hashCode());
		result = prime * result + (client == null ? 0 : client.hashCode());
		result = prime * result + (cloneURL == null ? 0 : cloneURL.hashCode());
		result = prime * result + id;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (tags == null ? 0 : tags.hashCode());
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
		final Repository other = (Repository) obj;
		if (account == null) {
			if (other.account != null) {
				return false;
			}
		} else if (!account.equals(other.account)) {
			return false;
		}
		if (branches == null) {
			if (other.branches != null) {
				return false;
			}
		} else if (!branches.equals(other.branches)) {
			return false;
		}
		if (client == null) {
			if (other.client != null) {
				return false;
			}
		} else if (!client.equals(other.client)) {
			return false;
		}
		if (cloneURL == null) {
			if (other.cloneURL != null) {
				return false;
			}
		} else if (!cloneURL.equals(other.cloneURL)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (tags == null) {
			if (other.tags != null) {
				return false;
			}
		} else if (!tags.equals(other.tags)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@JsonIgnore
	public List<Branch> getBranches() {
		return branches;
	}

	public void setBranches(final List<Branch> branches) {
		this.branches = branches;
	}

	@JsonIgnore
	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(final List<Tag> tags) {
		this.tags = tags;
	}

	@JsonProperty
	public Client getClient() {
		return client;
	}

	public void setClient(final Client client) {
		this.client = client;
	}
}

package nz.co.trineo.salesforce.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
	@Column
	private String authUrl;
	@OneToMany(cascade = CascadeType.ALL)
	private Set<Backup> backups;

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
	public String getAuthUrl() {
		return authUrl;
	}

	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}

	@JsonIgnore
	public Set<Backup> getBackups() {
		return backups;
	}

	public void setBackups(Set<Backup> backups) {
		this.backups = backups;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authUrl == null) ? 0 : authUrl.hashCode());
		result = prime * result + ((backups == null) ? 0 : backups.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((organizationType == null) ? 0 : organizationType.hashCode());
		result = prime * result + (sandbox ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Organization other = (Organization) obj;
		if (authUrl == null) {
			if (other.authUrl != null)
				return false;
		} else if (!authUrl.equals(other.authUrl))
			return false;
		if (backups == null) {
			if (other.backups != null)
				return false;
		} else if (!backups.equals(other.backups))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (organizationType == null) {
			if (other.organizationType != null)
				return false;
		} else if (!organizationType.equals(other.organizationType))
			return false;
		if (sandbox != other.sandbox)
			return false;
		return true;
	}
}

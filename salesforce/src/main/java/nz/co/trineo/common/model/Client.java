package nz.co.trineo.common.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.github.model.Repository;
import nz.co.trineo.salesforce.model.Organization;

@Entity
@Table(name = "client")
@JsonInclude(Include.NON_DEFAULT)
public class Client {
	@Id
	@GeneratedValue
	private long id;
	@Column
	private String name;
	@OneToMany
	private List<Organization> organizations;
	@OneToMany
	private List<Repository> repositories;

	@JsonProperty
	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@JsonIgnore
	public List<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(final List<Organization> organizations) {
		this.organizations = organizations;
	}

	@JsonIgnore
	public List<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(final List<Repository> repositories) {
		this.repositories = repositories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ id >>> 32);
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (organizations == null ? 0 : organizations.hashCode());
		result = prime * result + (repositories == null ? 0 : repositories.hashCode());
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
		final Client other = (Client) obj;
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
		if (organizations == null) {
			if (other.organizations != null) {
				return false;
			}
		} else if (!organizations.equals(other.organizations)) {
			return false;
		}
		if (repositories == null) {
			if (other.repositories != null) {
				return false;
			}
		} else if (!repositories.equals(other.repositories)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
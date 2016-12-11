package nz.co.trineo.github.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.salesforce.model.Organization;

@Entity
@Table(name = "branch")
@JsonInclude(Include.NON_DEFAULT)
public class Branch {
	@Id
	@GeneratedValue
	private long id;
	@Column
	private String sha;
	@Column
	private String name;
	@Column
	private String url;
	@ManyToOne
	@JoinColumn(name = "BRANCH_ID", nullable = false)
	private Repository repo;
	@OneToOne(mappedBy = "branch")
	private Organization org;

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
		final Branch other = (Branch) obj;
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
		if (repo == null) {
			if (other.repo != null) {
				return false;
			}
		} else if (!repo.equals(other.repo)) {
			return false;
		}
		if (sha == null) {
			if (other.sha != null) {
				return false;
			}
		} else if (!sha.equals(other.sha)) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}

	@JsonProperty
	public long getId() {
		return id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonIgnore
	public Organization getOrg() {
		return org;
	}

	@JsonIgnore
	public Repository getRepo() {
		return repo;
	}

	@JsonProperty
	public String getSha() {
		return sha;
	}

	@JsonProperty
	public String getUrl() {
		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ id >>> 32);
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (repo == null ? 0 : repo.hashCode());
		result = prime * result + (sha == null ? 0 : sha.hashCode());
		result = prime * result + (url == null ? 0 : url.hashCode());
		return result;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOrg(final Organization org) {
		this.org = org;
	}

	public void setRepo(final Repository repo) {
		this.repo = repo;
	}

	public void setSha(final String sha) {
		this.sha = sha;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.JSON_STYLE).setExcludeFieldNames("org", "repo")
				.build();
	}
}

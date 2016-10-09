package nz.co.trineo.github.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "tag")
@JsonInclude(Include.NON_DEFAULT)
public class Tag {
	@Id
	@GeneratedValue
	private long id;
	@Column
	private String sha;
	@Column
	private String name;
	@Column
	private String url;
	@Column
	private String tarballUrl;
	@Column
	private String zipballUrl;
	@ManyToOne
	@JoinColumn(name = "TAG_ID", nullable = false)
	private Repository repo;

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@JsonProperty
	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	@JsonProperty
	public String getSha() {
		return sha;
	}

	public void setSha(final String sha) {
		this.sha = sha;
	}

	@JsonProperty
	public String getTarballUrl() {
		return tarballUrl;
	}

	public void setTarballUrl(final String tarballUrl) {
		this.tarballUrl = tarballUrl;
	}

	@JsonProperty
	public String getZipballUrl() {
		return zipballUrl;
	}

	public void setZipballUrl(final String zipballUrl) {
		this.zipballUrl = zipballUrl;
	}

	@JsonIgnore
	public Repository getRepo() {
		return repo;
	}

	public void setRepo(final Repository repo) {
		this.repo = repo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ id >>> 32);
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (repo == null ? 0 : repo.hashCode());
		result = prime * result + (sha == null ? 0 : sha.hashCode());
		result = prime * result + (tarballUrl == null ? 0 : tarballUrl.hashCode());
		result = prime * result + (url == null ? 0 : url.hashCode());
		result = prime * result + (zipballUrl == null ? 0 : zipballUrl.hashCode());
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
		final Tag other = (Tag) obj;
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
		if (tarballUrl == null) {
			if (other.tarballUrl != null) {
				return false;
			}
		} else if (!tarballUrl.equals(other.tarballUrl)) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		if (zipballUrl == null) {
			if (other.zipballUrl != null) {
				return false;
			}
		} else if (!zipballUrl.equals(other.zipballUrl)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@JsonProperty
	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}
}

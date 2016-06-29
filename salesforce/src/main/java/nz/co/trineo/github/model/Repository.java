package nz.co.trineo.github.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.common.model.ConnectedAccount;

@Entity
@Table(name = "repo")
@JsonInclude(Include.NON_DEFAULT)
public class Repository {
	@Id
	private long id;
	@Column
	private String name;
	@Column
	private String cloneURL;
	@OneToOne
	private ConnectedAccount account;

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
}

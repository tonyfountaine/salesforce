package nz.co.trineo.git.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import nz.co.trineo.common.model.Credentals;

@Entity(name = "gitrepo")
@JsonInclude(Include.NON_DEFAULT)
public class GitRepo {
	@Id
	@GeneratedValue
	private int id;
	@Column
	private String name;
	@Column
	private String remote;
	@Embedded
	private Credentals credentals;

	public Credentals getCredentals() {
		return credentals;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getRemote() {
		return remote;
	}

	public void setCredentals(final Credentals credentals) {
		this.credentals = credentals;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setRemote(final String remote) {
		this.remote = remote;
	}
}

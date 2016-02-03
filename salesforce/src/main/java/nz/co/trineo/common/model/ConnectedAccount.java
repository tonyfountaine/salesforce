package nz.co.trineo.common.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity(name = "accounts")
@JsonInclude(Include.NON_DEFAULT)
public class ConnectedAccount {
	@Id
	@GeneratedValue
	private int id;
	@Column
	private String service;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Credentals credentals;
	@Column
	private String name;

	@Embedded
	private AccountToken token;

	@JsonProperty
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonProperty
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	@JsonProperty
	public Credentals getCredentals() {
		return credentals;
	}

	public void setCredentals(Credentals credentals) {
		this.credentals = credentals;
	}

	@JsonProperty
	public AccountToken getToken() {
		return token;
	}

	public void setToken(AccountToken accessToken) {
		this.token = accessToken;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

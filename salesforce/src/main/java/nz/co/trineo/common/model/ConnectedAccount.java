package nz.co.trineo.common.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((credentals == null) ? 0 : credentals.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
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
		ConnectedAccount other = (ConnectedAccount) obj;
		if (credentals == null) {
			if (other.credentals != null)
				return false;
		} else if (!credentals.equals(other.credentals))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}
}

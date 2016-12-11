package nz.co.trineo.common.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;

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
	@Embedded
	private Credentals credentals;
	@Column
	private String name;
	@Embedded
	private AccountToken token;

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
		final ConnectedAccount other = (ConnectedAccount) obj;
		if (credentals == null) {
			if (other.credentals != null) {
				return false;
			}
		} else if (!credentals.equals(other.credentals)) {
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
		if (service == null) {
			if (other.service != null) {
				return false;
			}
		} else if (!service.equals(other.service)) {
			return false;
		}
		if (token == null) {
			if (other.token != null) {
				return false;
			}
		} else if (!token.equals(other.token)) {
			return false;
		}
		return true;
	}

	@JsonProperty
	public Credentals getCredentals() {
		return credentals;
	}

	@JsonProperty
	public int getId() {
		return id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public String getService() {
		return service;
	}

	@JsonProperty
	public AccountToken getToken() {
		return token;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (credentals == null ? 0 : credentals.hashCode());
		result = prime * result + id;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (service == null ? 0 : service.hashCode());
		result = prime * result + (token == null ? 0 : token.hashCode());
		return result;
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

	public void setService(final String service) {
		this.service = service;
	}

	public void setToken(final AccountToken accessToken) {
		token = accessToken;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}

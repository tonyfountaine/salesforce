package nz.co.trineo.common.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity(name = "accounts")
public class ConnectedAccount {
	@Id
	@GeneratedValue
	private int id;
	@Column
	private String service;
	@OneToOne
	private Credentals credentals;

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
}

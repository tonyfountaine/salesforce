package nz.co.trineo.common.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@Table(name = "credentals")
@JsonInclude(Include.NON_DEFAULT)
public class Credentals {
	@Id
	@GeneratedValue
	private int id;
	@Column(name = "username")
	private String username;
	@Column(name = "password")
	private String password;

	@JsonProperty
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonProperty
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	@JsonProperty
	public void setPassword(String password) {
		this.password = password;
	}
}

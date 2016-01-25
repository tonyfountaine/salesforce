package nz.co.trineo.common.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

	/** Access token issued by the authorization server. */
	@Column
	private String accessToken;
	/**
	 * Refresh token which can be used to obtain new access tokens using the same authorization grant or {@code null}
	 * for none.
	 */
	@Column
	private String refreshToken;
	/**
	 * Expected expiration time in milliseconds based on {@link #setExpiresInSeconds} or {@code null} for none.
	 */
	@Column
	private Long expirationTimeMilliseconds;

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
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@JsonProperty
	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	@JsonProperty
	public Long getExpirationTimeMilliseconds() {
		return expirationTimeMilliseconds;
	}

	public void setExpirationTimeMilliseconds(Long expirationTimeMilliseconds) {
		this.expirationTimeMilliseconds = expirationTimeMilliseconds;
	}

	@JsonIgnore
	public Long getExpiresInSeconds() {
		return expirationTimeMilliseconds == null ? null
				: (expirationTimeMilliseconds - System.currentTimeMillis()) / 1000;
	}

	public void setExpiresInSeconds(final Long expiresIn) {
		setExpirationTimeMilliseconds(expiresIn == null ? null : System.currentTimeMillis() + expiresIn * 1000);
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

package nz.co.trineo.common.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountToken {
	@Column
	private String accessToken;
	@Column
	private String scpoe;
	@Column
	private String tokenType;
	@Column
	private String refreshToken;
	@Column
	private String instanceUrl;

	// {
	// "id":"https://login.salesforce.com/id/00D50000000IZ3ZEAW/00550000001fg5OAAQ",
	// "issued_at":"1296458209517",
	// "id_token": "eyJhb...h97hc",
	// "signature":"0/1Ldval/TIPf2tTgTKUAxRy44VwEJ7ffsFLMWFcNoA=",
	// }

	@JsonProperty("access_token")
	public String getAccessToken() {
		return accessToken;
	}

	@JsonProperty("access_token")
	public void setAccessToken(String access_token) {
		this.accessToken = access_token;
	}

	@JsonProperty
	public String getScpoe() {
		return scpoe;
	}

	public void setScpoe(String scpoe) {
		this.scpoe = scpoe;
	}

	@JsonProperty("token_type")
	public String getTokenType() {
		return tokenType;
	}

	@JsonProperty("token_type")
	public void setTokenType(String token_type) {
		this.tokenType = token_type;
	}

	@JsonProperty("refresh_token")
	public String getRefreshToken() {
		return refreshToken;
	}

	@JsonProperty("refresh_token")
	public void setRefreshToken(String refresh_token) {
		this.refreshToken = refresh_token;
	}

	@JsonProperty("instance_url")
	public String getInstanceUrl() {
		return instanceUrl;
	}

	@JsonProperty("instance_url")
	public void setInstanceUrl(String instanceUrl) {
		this.instanceUrl = instanceUrl;
	}
}
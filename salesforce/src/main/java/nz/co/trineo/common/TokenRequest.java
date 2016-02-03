package nz.co.trineo.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRequest {
	public String client_id;
	public String client_secret;
	public String code;
	public String redirect_uri;
	public String state;
	public String grant_type;

	@JsonProperty("client_id")
	public String getClientId() {
		return client_id;
	}

	@JsonProperty("client_id")
	public void setClientId(String client_id) {
		this.client_id = client_id;
	}

	@JsonProperty("client_secret")
	public String getClientSecret() {
		return client_secret;
	}

	@JsonProperty("client_secret")
	public void setClientSecret(String client_secret) {
		this.client_secret = client_secret;
	}

	@JsonProperty
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@JsonProperty("redirect_uri")
	public String getRedirectUri() {
		return redirect_uri;
	}

	@JsonProperty("redirect_uri")
	public void setRedirectUri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}

	@JsonProperty
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@JsonProperty("grant_type")
	public String getGrantType() {
		return grant_type;
	}

	@JsonProperty("grant_type")
	public void setGrantType(String grant_type) {
		this.grant_type = grant_type;
	}
}
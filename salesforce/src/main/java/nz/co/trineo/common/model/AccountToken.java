package nz.co.trineo.common.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.ToStringBuilder;

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
		final AccountToken other = (AccountToken) obj;
		if (accessToken == null) {
			if (other.accessToken != null) {
				return false;
			}
		} else if (!accessToken.equals(other.accessToken)) {
			return false;
		}
		if (instanceUrl == null) {
			if (other.instanceUrl != null) {
				return false;
			}
		} else if (!instanceUrl.equals(other.instanceUrl)) {
			return false;
		}
		if (refreshToken == null) {
			if (other.refreshToken != null) {
				return false;
			}
		} else if (!refreshToken.equals(other.refreshToken)) {
			return false;
		}
		if (scpoe == null) {
			if (other.scpoe != null) {
				return false;
			}
		} else if (!scpoe.equals(other.scpoe)) {
			return false;
		}
		if (tokenType == null) {
			if (other.tokenType != null) {
				return false;
			}
		} else if (!tokenType.equals(other.tokenType)) {
			return false;
		}
		return true;
	}

	@JsonProperty("access_token")
	public String getAccessToken() {
		return accessToken;
	}

	@JsonProperty("instance_url")
	public String getInstanceUrl() {
		return instanceUrl;
	}

	@JsonProperty("refresh_token")
	public String getRefreshToken() {
		return refreshToken;
	}

	@JsonProperty
	public String getScpoe() {
		return scpoe;
	}

	@JsonProperty("token_type")
	public String getTokenType() {
		return tokenType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (accessToken == null ? 0 : accessToken.hashCode());
		result = prime * result + (instanceUrl == null ? 0 : instanceUrl.hashCode());
		result = prime * result + (refreshToken == null ? 0 : refreshToken.hashCode());
		result = prime * result + (scpoe == null ? 0 : scpoe.hashCode());
		result = prime * result + (tokenType == null ? 0 : tokenType.hashCode());
		return result;
	}

	@JsonProperty("access_token")
	public void setAccessToken(final String access_token) {
		accessToken = access_token;
	}

	@JsonProperty("instance_url")
	public void setInstanceUrl(final String instanceUrl) {
		this.instanceUrl = instanceUrl;
	}

	@JsonProperty("refresh_token")
	public void setRefreshToken(final String refresh_token) {
		refreshToken = refresh_token;
	}

	public void setScpoe(final String scpoe) {
		this.scpoe = scpoe;
	}

	@JsonProperty("token_type")
	public void setTokenType(final String token_type) {
		tokenType = token_type;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
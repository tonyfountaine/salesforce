package nz.co.trineo.salesforce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SalesforceRequest {
	private Environment environment;
	private String orgUrl;

	@JsonProperty
	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@JsonProperty
	public String getOrgUrl() {
		return orgUrl;
	}

	public void setOrgUrl(String orgUrl) {
		this.orgUrl = orgUrl;
	}
}

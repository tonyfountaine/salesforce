package nz.co.trineo.configuration;

import java.io.File;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

public class AppConfiguration extends Configuration {
	@Valid
	@NotNull
	private DataSourceFactory database = new DataSourceFactory();

	@Valid
	private File gitDirectory;

	@Valid
	private File salesforceDirectory;

	@Valid
	private File backupDirectory;

	@Valid
	private String clientKey;

	@Valid
	private String clientSecret;

	@Valid
	private String apiVersion;

	@Valid
	private String trelloKey;

	@Valid
	private String trelloSecret;

	// @Valid
	// private String trelloToken;

	private String githubClientId;
	private String githubClientSecret;

	@JsonProperty("database")
	public DataSourceFactory getDataSourceFactory() {
		return database;
	}

	@JsonProperty("database")
	public void setDataSourceFactory(final DataSourceFactory factory) {
		database = factory;
	}

	@JsonProperty
	public File getGitDirectory() {
		return gitDirectory;
	}

	@JsonProperty
	public void setGitDirectory(final File gitDirectory) {
		this.gitDirectory = gitDirectory;
	}

	@JsonProperty
	public File getSalesforceDirectory() {
		return salesforceDirectory;
	}

	@JsonProperty
	public void setSalesforceDirectory(final File salesforceDirectory) {
		this.salesforceDirectory = salesforceDirectory;
	}

	@JsonProperty
	public File getBackupDirectory() {
		return backupDirectory;
	}

	@JsonProperty
	public void setBackupDirectory(final File backupDirectory) {
		this.backupDirectory = backupDirectory;
	}

	@JsonProperty
	public String getClientKey() {
		return clientKey;
	}

	@JsonProperty
	public void setClientKey(final String clientKey) {
		this.clientKey = clientKey;
	}

	@JsonProperty
	public String getClientSecret() {
		return clientSecret;
	}

	@JsonProperty
	public void setClientSecret(final String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@JsonProperty
	public String getApiVersion() {
		return apiVersion;
	}

	@JsonProperty
	public void setApiVersion(final String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@JsonProperty
	public String getTrelloKey() {
		return trelloKey;
	}

	@JsonProperty
	public void setTrelloKey(final String trelloKey) {
		this.trelloKey = trelloKey;
	}

	@JsonProperty
	public String getTrelloSecret() {
		return trelloSecret;
	}

	@JsonProperty
	public void setTrelloSecret(final String trelloSecret) {
		this.trelloSecret = trelloSecret;
	}

	// @JsonProperty
	// public String getTrelloToken() {
	// return trelloToken;
	// }
	//
	// @JsonProperty
	// public void setTrelloToken(String trelloToken) {
	// this.trelloToken = trelloToken;
	// }

	@JsonProperty
	public String getGithubClientId() {
		return githubClientId;
	}

	@JsonProperty
	public void setGithubClientId(final String githubClientId) {
		this.githubClientId = githubClientId;
	}

	@JsonProperty
	public String getGithubClientSecret() {
		return githubClientSecret;
	}

	@JsonProperty
	public void setGithubClientSecret(final String githubClientSecret) {
		this.githubClientSecret = githubClientSecret;
	}
}

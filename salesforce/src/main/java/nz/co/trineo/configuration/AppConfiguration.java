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
	private File githubDirectory;

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

	public void setDataSourceFactory(final DataSourceFactory factory) {
		database = factory;
	}

	@JsonProperty
	public File getGitDirectory() {
		return gitDirectory;
	}

	public void setGitDirectory(final File gitDirectory) {
		this.gitDirectory = gitDirectory;
	}

	@JsonProperty
	public File getSalesforceDirectory() {
		return salesforceDirectory;
	}

	public void setSalesforceDirectory(final File salesforceDirectory) {
		this.salesforceDirectory = salesforceDirectory;
	}

	@JsonProperty
	public File getBackupDirectory() {
		return backupDirectory;
	}

	public void setBackupDirectory(final File backupDirectory) {
		this.backupDirectory = backupDirectory;
	}

	@JsonProperty
	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(final String clientKey) {
		this.clientKey = clientKey;
	}

	@JsonProperty
	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(final String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@JsonProperty
	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(final String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@JsonProperty
	public String getTrelloKey() {
		return trelloKey;
	}

	public void setTrelloKey(final String trelloKey) {
		this.trelloKey = trelloKey;
	}

	@JsonProperty
	public String getTrelloSecret() {
		return trelloSecret;
	}

	public void setTrelloSecret(final String trelloSecret) {
		this.trelloSecret = trelloSecret;
	}

	// @JsonProperty
	// public String getTrelloToken() {
	// return trelloToken;
	// }
	//
	// public void setTrelloToken(String trelloToken) {
	// this.trelloToken = trelloToken;
	// }

	@JsonProperty
	public String getGithubClientId() {
		return githubClientId;
	}

	public void setGithubClientId(final String githubClientId) {
		this.githubClientId = githubClientId;
	}

	@JsonProperty
	public String getGithubClientSecret() {
		return githubClientSecret;
	}

	public void setGithubClientSecret(final String githubClientSecret) {
		this.githubClientSecret = githubClientSecret;
	}

	@JsonProperty
	public File getGithubDirectory() {
		return githubDirectory;
	}

	public void setGithubDirectory(final File githubDirectory) {
		this.githubDirectory = githubDirectory;
	}
}

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

	@JsonProperty("database")
	public DataSourceFactory getDataSourceFactory() {
		return database;
	}

	@JsonProperty("database")
	public void setDataSourceFactory(DataSourceFactory factory) {
		this.database = factory;
	}

	@JsonProperty
	public File getGitDirectory() {
		return gitDirectory;
	}

	@JsonProperty
	public void setGitDirectory(File gitDirectory) {
		this.gitDirectory = gitDirectory;
	}

	@JsonProperty
	public File getSalesforceDirectory() {
		return salesforceDirectory;
	}

	@JsonProperty
	public void setSalesforceDirectory(File salesforceDirectory) {
		this.salesforceDirectory = salesforceDirectory;
	}

	@JsonProperty
	public File getBackupDirectory() {
		return backupDirectory;
	}

	@JsonProperty
	public void setBackupDirectory(File backupDirectory) {
		this.backupDirectory = backupDirectory;
	}

	@JsonProperty
	public String getClientKey() {
		return clientKey;
	}

	@JsonProperty
	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	@JsonProperty
	public String getClientSecret() {
		return clientSecret;
	}

	@JsonProperty
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}

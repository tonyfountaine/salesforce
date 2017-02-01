package nz.co.trineo.salesforce.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.BackupStatus;

public class BackupView {
	private int id;
	private String name;
	private String retrieveId;
	private BackupStatus status;

	public BackupView(final Backup backup) {
		id = backup.getId();
		name = backup.getName();
		retrieveId = backup.getRetrieveId();
		status = backup.getStatus();
	}

	@JsonProperty
	public int getId() {
		return id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public String getRetrieveId() {
		return retrieveId;
	}

	@JsonProperty
	public BackupStatus getStatus() {
		return status;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setRetrieveId(final String retrieveId) {
		this.retrieveId = retrieveId;
	}

	public void setStatus(final BackupStatus status) {
		this.status = status;
	}
}

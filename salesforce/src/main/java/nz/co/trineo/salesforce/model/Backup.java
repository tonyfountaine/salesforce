package nz.co.trineo.salesforce.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@Table(name = "sfbackup")
@JsonInclude(Include.NON_DEFAULT)
public class Backup {
	@Id
	@GeneratedValue
	private int id;
	@Column
	private String name;
	@Column
	private String retrieveId;
	@Column
	private BackupStatus status;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getRetrieveId() {
		return retrieveId;
	}

	public void setRetrieveId(final String retrieveId) {
		this.retrieveId = retrieveId;
	}

	public BackupStatus getStatus() {
		return status;
	}

	public void setStatus(final BackupStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (retrieveId == null ? 0 : retrieveId.hashCode());
		result = prime * result + (status == null ? 0 : status.hashCode());
		return result;
	}

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
		final Backup other = (Backup) obj;
		if (id != other.id) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (retrieveId == null) {
			if (other.retrieveId != null) {
				return false;
			}
		} else if (!retrieveId.equals(other.retrieveId)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		return true;
	}
}

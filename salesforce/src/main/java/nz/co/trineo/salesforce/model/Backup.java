package nz.co.trineo.salesforce.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "backups")
public class Backup implements Serializable {
	private static final long serialVersionUID = -1530429454138118191L;

	@Id
	private String date;
	@Id
	private String organizationId;

	@JsonProperty
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@JsonProperty
	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}
}

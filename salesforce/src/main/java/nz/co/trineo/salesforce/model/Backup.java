package nz.co.trineo.salesforce.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "backups")
public class Backup {
	@Id
	@GeneratedValue
	private String id;
	@Column
	private String date;

	@JsonProperty
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}

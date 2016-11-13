package nz.co.trineo.salesforce.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "codeCoverageWarning")
@JsonInclude(Include.NON_DEFAULT)
public class CodeCoverageWarning {
	@Id
	@GeneratedValue
	private int dbId;
	@Column
	private String id;
	@Column
	private String message;
	@Column
	private String name;
	@Column
	private String namespace;

	@JsonProperty
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@JsonProperty
	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@JsonProperty
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
	}
}

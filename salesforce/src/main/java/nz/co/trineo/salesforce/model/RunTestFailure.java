package nz.co.trineo.salesforce.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "runTestFailure")
@JsonInclude(Include.NON_DEFAULT)
public class RunTestFailure extends RunTestMessage {
	@Column(length = 65536)
	private String message;
	@Column(length = 65536)
	private String stackTrace;
	@Column
	private String type;

	@JsonProperty
	public String getMessage() {
		return message;
	}

	@JsonProperty
	public String getStackTrace() {
		return stackTrace;
	}

	@JsonProperty
	public String getType() {
		return type;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public void setStackTrace(final String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public void setType(final String type) {
		this.type = type;
	}
}

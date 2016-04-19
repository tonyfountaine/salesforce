package nz.co.trineo.salesforce.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "codeLocation")
@JsonInclude(Include.NON_DEFAULT)
public class CodeLocation {
	@Id
	@GeneratedValue
	private int dbId;
	@Column
	private int column;
	@Column
	private int line;
	@Column
	private int numExecutions;
	@Column
	private double time;

	@JsonProperty
	public int getColumn() {
		return column;
	}

	public void setColumn(final int column) {
		this.column = column;
	}

	@JsonProperty
	public int getLine() {
		return line;
	}

	public void setLine(final int line) {
		this.line = line;
	}

	@JsonProperty
	public int getNumExecutions() {
		return numExecutions;
	}

	public void setNumExecutions(final int numExecutions) {
		this.numExecutions = numExecutions;
	}

	@JsonProperty
	public double getTime() {
		return time;
	}

	public void setTime(final double time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}

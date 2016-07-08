package nz.co.trineo.salesforce.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@Table(name = "runTestSuccess")
@JsonInclude(Include.NON_DEFAULT)
public class RunTestSuccess extends RunTestMessage {
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
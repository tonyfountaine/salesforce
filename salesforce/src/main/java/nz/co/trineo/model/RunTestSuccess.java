package nz.co.trineo.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@Table(name = "runTestSuccess")
@JsonInclude(Include.NON_DEFAULT)
public class RunTestSuccess extends RunTestMessage {
}

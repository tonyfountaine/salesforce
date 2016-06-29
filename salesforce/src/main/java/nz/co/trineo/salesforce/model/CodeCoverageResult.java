package nz.co.trineo.salesforce.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "codeCoverageResult")
@JsonInclude(Include.NON_DEFAULT)
public class CodeCoverageResult implements Comparable<CodeCoverageResult> {
	@Id
	@GeneratedValue
	private int dbId;
	@Column
	private String id;
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "resultId")
	private List<CodeLocation> locationsNotCovered = new ArrayList<>();
	@Column
	private String name;
	@Column
	private String namespace;
	@Column
	private int numLocations;
	@Column
	private int numLocationsNotCovered;
	@Column
	private String type;

	@JsonProperty
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@JsonProperty
	public List<CodeLocation> getLocationsNotCovered() {
		return locationsNotCovered;
	}

	public void setLocationsNotCovered(final List<CodeLocation> locationsNotCovered) {
		this.locationsNotCovered = locationsNotCovered;
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

	@JsonProperty
	public int getNumLocations() {
		return numLocations;
	}

	public void setNumLocations(final int numLocations) {
		this.numLocations = numLocations;
	}

	@JsonProperty
	public int getNumLocationsNotCovered() {
		return numLocationsNotCovered;
	}

	public void setNumLocationsNotCovered(final int numLocationsNotCovered) {
		this.numLocationsNotCovered = numLocationsNotCovered;
	}

	@JsonProperty
	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	@JsonProperty
	public float getPercent() {
		if (numLocations == 0) {
			return 100;
		}
		return (float) (numLocations - numLocationsNotCovered) / numLocations * 100;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(final CodeCoverageResult that) {
		int compare;
		if (namespace == null && that.namespace != null) {
			compare = -1;
		} else if (namespace != null && that.namespace == null) {
			compare = 1;
		} else if (namespace == null && that.namespace == null) {
			compare = 0;
		} else {
			compare = namespace.compareTo(that.namespace);
		}
		if (compare == 0) {
			compare = Float.compare(getPercent(), that.getPercent()) * -1;
		}
		if (compare == 0) {
			compare = name.compareTo(that.name);
		}
		return compare;
	}
}

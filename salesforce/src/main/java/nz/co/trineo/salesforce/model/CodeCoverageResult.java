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

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "codeCoverageResult")
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

	@JsonProperty
	public String getId() {
		return id;
	}

	@JsonProperty
	public List<CodeLocation> getLocationsNotCovered() {
		return locationsNotCovered;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public String getNamespace() {
		return namespace;
	}

	@JsonProperty
	public int getNumLocations() {
		return numLocations;
	}

	@JsonProperty
	public int getNumLocationsNotCovered() {
		return numLocationsNotCovered;
	}

	@JsonProperty
	public float getPercent() {
		if (numLocations == 0) {
			return 100;
		}
		return (float) (numLocations - numLocationsNotCovered) / numLocations * 100;
	}

	@JsonProperty
	public String getType() {
		return type;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setLocationsNotCovered(final List<CodeLocation> locationsNotCovered) {
		this.locationsNotCovered = locationsNotCovered;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
	}

	public void setNumLocations(final int numLocations) {
		this.numLocations = numLocations;
	}

	public void setNumLocationsNotCovered(final int numLocationsNotCovered) {
		this.numLocationsNotCovered = numLocationsNotCovered;
	}

	public void setType(final String type) {
		this.type = type;
	}
}

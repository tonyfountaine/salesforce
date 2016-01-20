package nz.co.trineo.diff.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity(name = "diff")
@JsonInclude(Include.NON_DEFAULT)
public class Diff {
	@Id
	@GeneratedValue
	private int id;
	@ElementCollection
	@MapKeyColumn(name = "filename")
	@Column(name = "addedContent", length = 32768)
	@CollectionTable(name = "diffData")
	private Map<String, String> added = new HashMap<>(); // filename to content
	@ElementCollection
	@MapKeyColumn(name = "filename")
	@Column(name = "removedContent", length = 32768)
	@CollectionTable(name = "diffData")
	private Map<String, String> removed = new HashMap<>(); // filename to
															// content
	@ElementCollection
	@MapKeyColumn(name = "filename")
	@Column(name = "modifiedContent", length = 32768)
	@CollectionTable(name = "diffData")
	private Map<String, String> modified = new HashMap<>(); // filename to
															// unified diff

	@JsonProperty
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonProperty
	public Map<String, String> getAdded() {
		return added;
	}

	public void setAdded(Map<String, String> added) {
		this.added = added;
	}

	@JsonProperty
	public Map<String, String> getRemoved() {
		return removed;
	}

	public void setRemoved(Map<String, String> removed) {
		this.removed = removed;
	}

	@JsonProperty
	public Map<String, String> getModified() {
		return modified;
	}

	public void setModified(Map<String, String> modified) {
		this.modified = modified;
	}
}

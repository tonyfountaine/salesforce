package nz.co.trineo.repo.view;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.model.Repository;
import nz.co.trineo.model.RepositoryType;

public class RepositoryView {
	private int id;
	private String name;
	private String cloneURL;
	private List<BranchView> branches;
	private RepositoryType type;

	public RepositoryView(final Repository repository) {
		id = repository.getId();
		name = repository.getName();
		cloneURL = repository.getCloneURL();
		branches = new ArrayList<>();
		repository.getBranches().forEach(b -> {
			final BranchView bv = new BranchView(b);
			branches.add(bv);
		});
		type = repository.getType();
	}

	@JsonProperty
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty
	public String getCloneURL() {
		return cloneURL;
	}

	public void setCloneURL(String cloneURL) {
		this.cloneURL = cloneURL;
	}

	@JsonProperty
	public List<BranchView> getBranches() {
		return branches;
	}

	public void setBranches(List<BranchView> branches) {
		this.branches = branches;
	}

	@JsonProperty
	public RepositoryType getType() {
		return type;
	}

	public void setType(RepositoryType type) {
		this.type = type;
	}
}
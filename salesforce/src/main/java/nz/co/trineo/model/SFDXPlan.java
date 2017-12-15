package nz.co.trineo.model;

import java.util.ArrayList;
import java.util.List;

public class SFDXPlan {
	private String sobject;
	private boolean saveRefs;
	private boolean resolveRefs;
	private final List<String> files = new ArrayList<>();

	public String getSobject() {
		return sobject;
	}

	public void setSobject(String sobject) {
		this.sobject = sobject;
	}

	public boolean isSaveRefs() {
		return saveRefs;
	}

	public void setSaveRefs(boolean saveRefs) {
		this.saveRefs = saveRefs;
	}

	public boolean isResolveRefs() {
		return resolveRefs;
	}

	public void setResolveRefs(boolean resolveRefs) {
		this.resolveRefs = resolveRefs;
	}

	public List<String> getFiles() {
		return files;
	}

	public void addFile(final String file) {
		files.add(file);
	}
}

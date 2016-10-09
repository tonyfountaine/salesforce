package nz.co.trineo.salesforce.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dropwizard.views.View;
import nz.co.trineo.git.model.GitDiff;

public class CompareView extends View {

	private final List<GitDiff> compare;
	private final Map<String, List<GitDiff>> diffMap = new HashMap<>();

	public CompareView(final List<GitDiff> list) {
		super("/compare.ftl");
		compare = list;
		list.forEach(d -> {
			final String path = "/dev/null".equals(d.getPathA()) ? d.getPathB() : d.getPathA();
			if (!"package.xml".equals(path)) {
				final String[] parts = path.split("/");
				if (!diffMap.containsKey(parts[0])) {
					diffMap.put(parts[0], new ArrayList<>());
				}
				diffMap.get(parts[0]).add(d);
			}
		});
	}

	public List<GitDiff> getCompare() {
		return compare;
	}

	public Map<String, List<GitDiff>> getDiffMap() {
		return diffMap;
	}
}

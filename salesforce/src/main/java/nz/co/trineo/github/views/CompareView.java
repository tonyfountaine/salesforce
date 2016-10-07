package nz.co.trineo.github.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dropwizard.views.View;
import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.salesforce.model.TreeNode;

public class CompareView extends View {

	private final List<GitDiff> compare;
	private final Map<String, List<GitDiff>> diffMap = new HashMap<>();
	private final TreeNode rootNode;

	public CompareView(final List<GitDiff> list, final TreeNode diffTree) {
		super("/githubcompare.ftl");
		compare = list;
		rootNode = diffTree;
		list.forEach(d -> {
			final String path = "/dev/null".equals(d.getPathA()) ? d.getPathB() : d.getPathA();
			if (!"package.xml".equals(path)) {
				final String key = path.replaceAll("/", "_");
				if (!diffMap.containsKey(key)) {
					diffMap.put(key, new ArrayList<>());
				}
				diffMap.get(key).add(d);
			}
		});
	}

	public List<GitDiff> getCompare() {
		return compare;
	}

	public Map<String, List<GitDiff>> getDiffMap() {
		return diffMap;
	}

	public TreeNode getRootNode() {
		return rootNode;
	}
}

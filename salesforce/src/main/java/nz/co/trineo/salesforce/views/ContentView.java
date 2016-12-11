package nz.co.trineo.salesforce.views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.dropwizard.views.View;
import nz.co.trineo.salesforce.model.CodeCoverageResult;

public class ContentView extends View {

	private final List<String> lines;
	private final Set<Integer> coverage;
	private final CodeCoverageResult result;

	public ContentView(final List<String> lines, final CodeCoverageResult result) {
		super("/content.ftl");
		this.lines = lines;
		coverage = new HashSet<>();
		this.result = result;
		if (result != null) {
			result.getLocationsNotCovered().forEach(cl -> {
				coverage.add(cl.getLine());
			});
		}
	}

	public Set<Integer> getCoverage() {
		return coverage;
	}

	public List<String> getLines() {
		return lines;
	}

	public CodeCoverageResult getResult() {
		return result;
	}
}

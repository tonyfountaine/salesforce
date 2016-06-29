package nz.co.trineo.salesforce.views;

import java.util.Collections;
import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.salesforce.model.CodeCoverageResult;

public class CoverageView extends View {
	private final List<CodeCoverageResult> coverage;

	public CoverageView(final List<CodeCoverageResult> coverage) {
		super("/coverage.ftl");
		this.coverage = coverage;
		if (coverage != null) {
			Collections.sort(coverage);
		}
	}

	public List<CodeCoverageResult> getCoverage() {
		return coverage;
	}
}

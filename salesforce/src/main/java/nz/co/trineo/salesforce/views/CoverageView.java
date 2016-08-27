package nz.co.trineo.salesforce.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.salesforce.model.CodeCoverageResult;

public class CoverageView extends View {
	private final List<CodeCoverageResult> coverage = new ArrayList<>();

	public CoverageView(final List<CodeCoverageResult> coverage) {
		super("/coverage.ftl");
		if (coverage != null) {
			this.coverage.addAll(coverage);
			Collections.sort(this.coverage);
		}
	}

	public List<CodeCoverageResult> getCoverage() {
		return coverage;
	}
}

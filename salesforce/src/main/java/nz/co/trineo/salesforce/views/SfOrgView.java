package nz.co.trineo.salesforce.views;

import java.util.List;
import java.util.Set;

import io.dropwizard.views.View;
import nz.co.trineo.salesforce.model.Organization;
import nz.co.trineo.salesforce.model.RunTestsResult;

public class SfOrgView extends View {
	private final Organization org;
	private final Set<String> backups;
	private final List<RunTestsResult> tests;

	public SfOrgView(final Organization org, final Set<String> backups, final List<RunTestsResult> tests) {
		super("/sforg.ftl");
		this.org = org;
		this.backups = backups;
		this.tests = tests;
	}

	public Organization getOrg() {
		return org;
	}

	public Set<String> getBackups() {
		return backups;
	}

	public List<RunTestsResult> getTests() {
		return tests;
	}
}

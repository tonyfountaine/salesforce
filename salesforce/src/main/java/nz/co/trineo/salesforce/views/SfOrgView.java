package nz.co.trineo.salesforce.views;

import java.util.Set;

import io.dropwizard.views.View;
import nz.co.trineo.salesforce.model.Organization;

public class SfOrgView extends View {
	private final Organization org;
	private final Set<String> backups;

	public SfOrgView(final Organization org, final Set<String> backups) {
		super("/sforg.ftl");
		this.org = org;
		this.backups = backups;
	}

	public Organization getOrg() {
		return org;
	}

	public Set<String> getBackups() {
		return backups;
	}
}

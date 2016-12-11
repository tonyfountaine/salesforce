package nz.co.trineo.salesforce.views;

import java.util.List;

import io.dropwizard.views.View;
import nz.co.trineo.common.model.Client;
import nz.co.trineo.github.model.Branch;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.Organization;
import nz.co.trineo.salesforce.model.RunTestsResult;

public class SfOrgView extends View {
	private final Organization org;
	private final List<Backup> backups;
	private final List<RunTestsResult> tests;
	private final List<Client> clients;
	private final List<Branch> branches;

	public SfOrgView(final Organization org, final List<Backup> backups, final List<RunTestsResult> tests,
			final List<Client> clients, final List<Branch> branches) {
		super("/sforg.ftl");
		this.org = org;
		this.backups = backups;
		this.tests = tests;
		this.clients = clients;
		this.branches = branches;
	}

	public List<Backup> getBackups() {
		return backups;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public List<Client> getClients() {
		return clients;
	}

	public Organization getOrg() {
		return org;
	}

	public List<RunTestsResult> getTests() {
		return tests;
	}
}

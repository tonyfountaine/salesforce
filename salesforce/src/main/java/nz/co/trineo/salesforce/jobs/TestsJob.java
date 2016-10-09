package nz.co.trineo.salesforce.jobs;

import org.quartz.JobDataMap;

import nz.co.anzac.dropwizard.quartz.Job;
import nz.co.anzac.dropwizard.quartz.annotation.Schedule;
import nz.co.trineo.salesforce.SalesforceService;

@Schedule(cron = "0 0 1 * * ?")
public class TestsJob extends Job {

	@Override
	protected void execute() throws Exception {
		final JobDataMap jdm = context.getMergedJobDataMap();
		final SalesforceService salesforceService = (SalesforceService) jdm.get("salesforceService");
		final String orgId = jdm.getString("orgId");
		salesforceService.runTests(orgId, null);
	}
}

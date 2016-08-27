package nz.co.trineo.salesforce.jobs;

import org.quartz.JobDataMap;

import nz.co.anzac.dropwizard.quartz.Job;
import nz.co.anzac.dropwizard.quartz.annotation.Schedule;
import nz.co.trineo.salesforce.SalesforceException;
import nz.co.trineo.salesforce.SalesforceService;

@Schedule(cron = "0 0 0 * * ?")
public class BackupJob extends Job {
	// private static final Log log = LogFactory.getLog(BackupJob.class);

	@Override
	public void execute() throws SalesforceException {
		final JobDataMap jdm = context.getMergedJobDataMap();
		final SalesforceService salesforceService = (SalesforceService) jdm.get("salesforceService");
		final String orgId = jdm.getString("orgId");
		salesforceService.createBackup(orgId);
	}
}

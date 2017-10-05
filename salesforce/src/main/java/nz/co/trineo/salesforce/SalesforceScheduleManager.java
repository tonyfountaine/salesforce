package nz.co.trineo.salesforce;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.jvnet.hk2.annotations.Service;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import io.dropwizard.lifecycle.Managed;
import nz.co.anzac.dropwizard.quartz.Job;
import nz.co.anzac.dropwizard.quartz.QuartzManager;
import nz.co.anzac.dropwizard.quartz.annotation.Schedule;
import nz.co.trineo.salesforce.jobs.BackupJob;
import nz.co.trineo.salesforce.jobs.TestsJob;

@Service
public class SalesforceScheduleManager implements Managed {
	private static final Log log = LogFactory.getLog(QuartzManager.class);
	private Scheduler scheduler;
	private final SessionFactory sessionFactory;
	private final SalesforceService salesforceService;

	@Inject
	public SalesforceScheduleManager(final SessionFactory sessionFactory, final SalesforceService salesforceService) {
		super();
		this.sessionFactory = sessionFactory;
		this.salesforceService = salesforceService;
	}

	private void scheduleBackupJob(final String orgId) throws SchedulerException {
		scheduleJob(orgId, BackupJob.class);
	}

	private void scheduleBackupJobs() {
		final Session session = sessionFactory.openSession();
		try {
			ManagedSessionContext.bind(session);
			final Transaction transaction = session.beginTransaction();
			try {
				salesforceService.listOrgs().forEach(o -> {
					try {
						scheduleBackupJob(o.getId());
					} catch (final SchedulerException e) {
						log.error("Unable to schedule backup for " + o.getName(), e);
					}
				});
				transaction.commit();
			} catch (final Exception e) {
				transaction.rollback();
				throw new RuntimeException(e);
			}
		} finally {
			session.close();
			ManagedSessionContext.unbind(sessionFactory);
		}
	}

	private <J extends Job> void scheduleJob(final String orgId, final Class<J> jobClass) throws SchedulerException {
		final Schedule annotation = jobClass.getAnnotation(Schedule.class);
		final String cron = annotation.cron();
		final CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
		final Trigger trigger = TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).build();
		final JobDataMap jdm = new JobDataMap();
		jdm.put("sessionFactory", sessionFactory);
		jdm.put("salesforceService", salesforceService);
		jdm.put("orgId", orgId);
		final JobBuilder jobBuilder = JobBuilder.newJob(jobClass).setJobData(jdm);
		scheduler.scheduleJob(jobBuilder.build(), trigger);

		log.info(String.format("    %-21s %s", cron, jobClass.getCanonicalName()));
	}

	private void scheduleTestJob(final String orgId) throws SchedulerException {
		scheduleJob(orgId, TestsJob.class);
	}

	private void scheduleTestJobs() {
		final Session session = sessionFactory.openSession();
		try {
			ManagedSessionContext.bind(session);
			final Transaction transaction = session.beginTransaction();
			try {
				salesforceService.listOrgs().forEach(o -> {
					try {
						scheduleTestJob(o.getId());
					} catch (final SchedulerException e) {
						log.error("Unable to schedule backup for " + o.getName(), e);
					}
				});
				transaction.commit();
			} catch (final Exception e) {
				transaction.rollback();
				throw new RuntimeException(e);
			}
		} finally {
			session.close();
			ManagedSessionContext.unbind(sessionFactory);
		}
	}

	@Override
	public void start() throws Exception {
		scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.start();
		scheduleBackupJobs();
		scheduleTestJobs();
	}

	@Override
	public void stop() throws Exception {
		log.info("shutting down scheduler");
		scheduler.shutdown(true);
	}
}

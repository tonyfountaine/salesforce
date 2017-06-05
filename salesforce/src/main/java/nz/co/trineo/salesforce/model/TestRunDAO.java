package nz.co.trineo.salesforce.model;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import io.dropwizard.hibernate.AbstractDAO;

public class TestRunDAO extends AbstractDAO<RunTestsResult> {
	@Inject
	public TestRunDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public void delete(final int id) {
		final RunTestsResult org = get(id);
		currentSession().delete(org);
	}

	public RunTestsResult find(final String runId) {
		return uniqueResult(currentSession().createCriteria(getEntityClass()).add(Restrictions.eq("apexLogId", runId)));
	}

	@Override
	public RunTestsResult get(final Serializable id) {
		final RunTestsResult organization = super.get(id);
		return organization;
	}

	public List<RunTestsResult> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	@Override
	public RunTestsResult persist(final RunTestsResult entity) throws HibernateException {
		return super.persist(entity);
	}
}

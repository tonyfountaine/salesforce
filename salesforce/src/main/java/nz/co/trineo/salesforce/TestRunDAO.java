package nz.co.trineo.salesforce;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.salesforce.model.RunTestsResult;

public class TestRunDAO extends AbstractDAO<RunTestsResult> {
	public TestRunDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public RunTestsResult get(final Serializable id) {
		final RunTestsResult organization = super.get(id);
		return organization;
	}

	public RunTestsResult find(final String runId) {
		return uniqueResult(currentSession().createCriteria(getEntityClass()).add(Restrictions.eq("apexLogId", runId)));
	}

	@Override
	public RunTestsResult persist(final RunTestsResult entity) throws HibernateException {
		return super.persist(entity);
	}

	public void delete(final String id) {
		final RunTestsResult org = get(id);
		currentSession().delete(org);
	}

	public List<RunTestsResult> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}
}

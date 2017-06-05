package nz.co.trineo.common;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.jvnet.hk2.annotations.Service;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.model.ConnectedAccount;

@Service
public class AccountDAO extends AbstractDAO<ConnectedAccount> {
	@Inject
	public AccountDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public void delete(final int id) {
		final ConnectedAccount account = get(id);
		currentSession().delete(account);
	}

	@Override
	public ConnectedAccount get(final Serializable id) {
		return super.get(id);
	}

	public List<ConnectedAccount> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	public List<ConnectedAccount> listByService(final String service) {
		return list(currentSession().createCriteria(getEntityClass()).add(Restrictions.eq("service", service)));
	}

	@Override
	public ConnectedAccount persist(final ConnectedAccount entity) throws HibernateException {
		return super.persist(entity);
	}
}

package nz.co.trineo.common;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.common.model.ConnectedAccount;

public class AccountDAO extends AbstractDAO<ConnectedAccount> {
	public AccountDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public ConnectedAccount get(final Serializable id) {
		return super.get(id);
	}

	@Override
	public ConnectedAccount persist(final ConnectedAccount entity) throws HibernateException {
		return super.persist(entity);
	}

	public List<ConnectedAccount> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	public void delete(final int id) {
		final ConnectedAccount account = get(id);
		currentSession().delete(account);
	}
}

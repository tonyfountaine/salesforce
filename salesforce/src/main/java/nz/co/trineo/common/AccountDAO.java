package nz.co.trineo.common;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.common.model.ConnectedAccount;

public class AccountDAO extends AbstractDAO<ConnectedAccount> {
	public AccountDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public ConnectedAccount get(Serializable id) {
		return super.get(id);
	}

	@Override
	public ConnectedAccount persist(ConnectedAccount entity) throws HibernateException {
		if (entity.getCredentals() != null) {
			currentSession().saveOrUpdate(entity.getCredentals());
		}
		return super.persist(entity);
	}

	public List<ConnectedAccount> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	public void delete(int id) {
		final ConnectedAccount account = get(id);
		currentSession().delete(account.getCredentals());
		currentSession().delete(account);
	}
}

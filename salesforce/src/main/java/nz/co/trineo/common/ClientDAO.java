package nz.co.trineo.common;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.common.model.Client;

public class ClientDAO extends AbstractDAO<Client> {
	public ClientDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Client get(final Serializable id) {
		return super.get(id);
	}

	@Override
	public Client persist(final Client entity) throws HibernateException {
		return super.persist(entity);
	}

	public List<Client> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	public void delete(final int id) {
		final Client account = get(id);
		currentSession().delete(account);
	}
}

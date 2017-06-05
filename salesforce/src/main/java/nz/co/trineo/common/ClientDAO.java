package nz.co.trineo.common;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.common.model.Client;

public class ClientDAO extends AbstractDAO<Client> {
	@Inject
	public ClientDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public void delete(final long id) {
		final Client account = get(id);
		currentSession().delete(account);
	}

	@Override
	public Client get(final Serializable id) {
		return super.get(id);
	}

	public List<Client> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	@Override
	public Client persist(final Client entity) throws HibernateException {
		return super.persist(entity);
	}
}

package nz.co.trineo.salesforce.model;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;

public class BackupDAO extends AbstractDAO<Backup> {
	@Inject
	public BackupDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public void delete(final Serializable id) {
		final Backup org = get(id);
		currentSession().delete(org);
	}

	@Override
	public Backup get(final Serializable id) {
		final Backup organization = super.get(id);
		return organization;
	}

	public List<Backup> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	@Override
	public Backup persist(final Backup entity) throws HibernateException {
		return super.persist(entity);
	}
}

package nz.co.trineo.salesforce;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.salesforce.model.Backup;

public class BackupDAO extends AbstractDAO<Backup> {
	public BackupDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Backup get(final Serializable id) {
		final Backup organization = super.get(id);
		return organization;
	}

	@Override
	public Backup persist(final Backup entity) throws HibernateException {
		return super.persist(entity);
	}

	public void delete(final Serializable id) {
		final Backup org = get(id);
		currentSession().delete(org);
	}

	public List<Backup> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}
}

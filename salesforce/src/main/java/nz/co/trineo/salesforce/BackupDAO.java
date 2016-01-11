package nz.co.trineo.salesforce;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.salesforce.model.Backup;
import nz.co.trineo.salesforce.model.Organization;

public class BackupDAO extends AbstractDAO<Backup> {
	public BackupDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Backup get(final Organization org, final String date) {
		return uniqueResult(currentSession().createCriteria(Backup.class).add(Restrictions.eq("organization", org))
				.add(Restrictions.eq("date", date)));
	}

	@Override
	public Backup persist(Backup entity) throws HibernateException {
		return super.persist(entity);
	}

	public void delete(final Backup entity) {
		currentSession().delete(entity);
	}
}

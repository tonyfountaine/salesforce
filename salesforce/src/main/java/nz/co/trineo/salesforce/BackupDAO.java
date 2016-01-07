package nz.co.trineo.salesforce;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.salesforce.model.Backup;

public class BackupDAO extends AbstractDAO<Backup> {
	public BackupDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Backup get(final String orgId, final String date) {
		return uniqueResult(currentSession().createCriteria(Backup.class).add(Restrictions.eq("organizationId", orgId))
				.add(Restrictions.eq("date", date)));
	}

	@Override
	public Backup persist(Backup entity) throws HibernateException {
		return super.persist(entity);
	}

	public List<Backup> listBackupsByOrg(final String orgId) {
		return list(currentSession().createCriteria(Backup.class).add(Restrictions.eq("organizationId", orgId)));
	}

	public List<Backup> listBackupsByDate(final String date) {
		return list(currentSession().createCriteria(Backup.class).add(Restrictions.eq("date", date)));
	}
}

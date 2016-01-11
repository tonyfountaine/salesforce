package nz.co.trineo.salesforce;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.salesforce.model.Organization;

public class OrganizationDAO extends AbstractDAO<Organization> {
	public OrganizationDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Organization get(Serializable id) {
		final Organization organization = super.get(id);
		initialize(organization.getBackups());
		return organization;
	}

	@Override
	public Organization persist(Organization entity) throws HibernateException {
		return super.persist(entity);
	}

	public void delete(final String id) {
		final Organization org = get(id);
		currentSession().delete(org);
	}
}

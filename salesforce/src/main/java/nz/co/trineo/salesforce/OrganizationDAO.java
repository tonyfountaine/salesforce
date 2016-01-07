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
		return super.get(id);
	}

	@Override
	public Organization persist(Organization entity) throws HibernateException {
		return super.persist(entity);
	}
}

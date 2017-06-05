package nz.co.trineo.salesforce;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.jvnet.hk2.annotations.Service;

import com.google.common.base.Objects;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.model.ConnectedAccount;
import nz.co.trineo.model.Organization;

@Service
public class OrganizationDAO extends AbstractDAO<Organization> {
	@Inject
	public OrganizationDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public void delete(final String id) {
		final Organization org = get(id);
		currentSession().delete(org);
	}

	public Organization findOrganization(final ConnectedAccount account) {
		return uniqueResult(currentSession().createCriteria(getEntityClass()).add(Restrictions.eq("account", account)));
	}

	@Override
	public Organization get(final Serializable id) {
		final Organization organization = super.get(id);
		return organization;
	}

	public List<Organization> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	@Override
	public Organization persist(final Organization entity) throws HibernateException {
		if (entity.getBranch() != null && !Objects.equal(entity.getBranch().getOrg(), entity)) {
			entity.getBranch().setOrg(entity);
			// currentSession().persist(entity.getBranch());
		}
		return super.persist(entity);
	}
}

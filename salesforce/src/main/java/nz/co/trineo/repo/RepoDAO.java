package nz.co.trineo.repo;

import static org.hibernate.criterion.Restrictions.eq;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import com.google.common.base.Objects;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.repo.model.Branch;
import nz.co.trineo.repo.model.Repository;
import nz.co.trineo.repo.model.RepositoryType;

public class RepoDAO extends AbstractDAO<Repository> {
	public RepoDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public void delete(final int id) {
		final Repository account = get(id);
		currentSession().delete(account);
	}

	@Override
	public Repository get(final Serializable id) {
		return super.get(id);
	}

	public Branch getBranch(final long id) {
		return (Branch) currentSession().get(Branch.class, id);
	}

	public Repository getByName(final String name) {
		return uniqueResult(currentSession().createCriteria(getEntityClass()).add(eq("name", name)));
	}

	public List<Repository> listByType(final RepositoryType type) {
		return list(currentSession().createCriteria(getEntityClass()).add(eq("type", type)));
	}

	@Override
	public Repository persist(final Repository entity) throws HibernateException {
		if (entity.getBranches() != null) {
			entity.getBranches().forEach(b -> {
				currentSession().persist(b);
				if (b.getOrg() != null && Objects.equal(b.getOrg().getBranch(), b)) {
					b.getOrg().setBranch(b);
					// currentSession().persist(b.getOrg());
				}
			});
		}
		if (entity.getTags() != null) {
			entity.getTags().forEach(t -> {
				currentSession().persist(t);
			});
		}
		return super.persist(entity);
	}
}

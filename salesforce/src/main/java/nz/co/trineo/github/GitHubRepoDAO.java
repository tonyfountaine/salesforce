package nz.co.trineo.github;

import static org.hibernate.criterion.Restrictions.eq;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.github.model.Branch;
import nz.co.trineo.github.model.Repository;

public class GitHubRepoDAO extends AbstractDAO<Repository> {
	public GitHubRepoDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Repository get(final Serializable id) {
		return super.get(id);
	}

	@Override
	public Repository persist(final Repository entity) throws HibernateException {
		entity.getBranches().forEach(b -> {
			currentSession().persist(b);
		});
		entity.getTags().forEach(t -> {
			currentSession().persist(t);
		});
		return super.persist(entity);
	}

	public List<Repository> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	public void delete(final int id) {
		final Repository account = get(id);
		currentSession().delete(account);
	}

	public Repository getByName(final String name) {
		return uniqueResult(currentSession().createCriteria(getEntityClass()).add(eq("name", name)));
	}

	public Branch getBranch(final long id) {
		return (Branch) currentSession().get(Branch.class, id);
	}
}

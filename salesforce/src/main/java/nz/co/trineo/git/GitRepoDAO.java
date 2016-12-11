package nz.co.trineo.git;

import static org.hibernate.criterion.Restrictions.eq;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.git.model.GitRepo;

public class GitRepoDAO extends AbstractDAO<GitRepo> {
	public GitRepoDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public void delete(final int id) {
		final GitRepo account = get(id);
		currentSession().delete(account);
	}

	@Override
	public GitRepo get(final Serializable id) {
		return super.get(id);
	}

	public GitRepo getByName(final String name) {
		return uniqueResult(currentSession().createCriteria(getEntityClass()).add(eq("name", name)));
	}

	public List<GitRepo> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	@Override
	public GitRepo persist(final GitRepo entity) throws HibernateException {
		return super.persist(entity);
	}
}

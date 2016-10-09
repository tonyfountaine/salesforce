package nz.co.trineo.git;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.git.model.GitProcess;

public class GitProcessDAO extends AbstractDAO<GitProcess> {

	public GitProcessDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	protected GitProcess get(final Serializable id) {
		return super.get(id);
	}

	@Override
	protected GitProcess persist(final GitProcess entity) throws HibernateException {
		return super.persist(entity);
	}
}

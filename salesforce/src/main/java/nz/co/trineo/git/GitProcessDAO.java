package nz.co.trineo.git;

import java.io.Serializable;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.jvnet.hk2.annotations.Service;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.model.GitProcess;

@Service
public class GitProcessDAO extends AbstractDAO<GitProcess> {

	@Inject
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

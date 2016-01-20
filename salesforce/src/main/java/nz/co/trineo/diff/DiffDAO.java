package nz.co.trineo.diff;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.diff.model.Diff;

public class DiffDAO extends AbstractDAO<Diff> {

	public DiffDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Diff get(Serializable id) {
		return super.get(id);
	}

	@Override
	public Diff persist(Diff entity) throws HibernateException {
		return super.persist(entity);
	}
}

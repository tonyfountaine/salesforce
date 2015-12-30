package nz.co.trineo.common;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.common.model.Credentals;

public class CredentalsDAO extends AbstractDAO<Credentals> {
	public CredentalsDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Credentals get(Serializable id) {
		return super.get(id);
	}

	@Override
	public Credentals persist(Credentals entity) throws HibernateException {
		return super.persist(entity);
	}
}

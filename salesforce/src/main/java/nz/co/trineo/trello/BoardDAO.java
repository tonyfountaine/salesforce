package nz.co.trineo.trello;

import static org.hibernate.criterion.Restrictions.eq;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import io.dropwizard.hibernate.AbstractDAO;
import nz.co.trineo.trello.model.Board;

public class BoardDAO extends AbstractDAO<Board> {
	public BoardDAO(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Board get(final Serializable id) {
		return super.get(id);
	}

	@Override
	public Board persist(final Board entity) throws HibernateException {
		return super.persist(entity);
	}

	public List<Board> listAll() {
		return list(currentSession().createCriteria(getEntityClass()));
	}

	public void delete(final int id) {
		final Board account = get(id);
		currentSession().delete(account);
	}

	public Board getByName(final String name) {
		return uniqueResult(currentSession().createCriteria(getEntityClass()).add(eq("name", name)));
	}
}

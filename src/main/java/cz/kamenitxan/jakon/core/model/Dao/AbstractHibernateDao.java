package cz.kamenitxan.jakon.core.model.Dao;

import org.hibernate.Session;

import java.io.Serializable;
import java.util.List;

public class AbstractHibernateDao<T extends Serializable> {

	private Class<T> clazz;

	public AbstractHibernateDao(Class<T> clazz) {
		this.clazz = clazz;
	}

	public T findOne(int id) {
		return (T) getCurrentSession().get(clazz, id);
	}

	public List<T> findAll() {
		Session session = getCurrentSession();
		session.beginTransaction();
		List<T> all = session.createQuery("from " + clazz.getName()).list();
		session.getTransaction().commit();
		return all;
	}

	public void create(T entity) {
		getCurrentSession().persist(entity);
	}

	public void update(T entity) {
		getCurrentSession().merge(entity);
	}

	public void delete(T entity) {
		getCurrentSession().delete(entity);
	}

	public void deleteById(int entityId) {
		T entity = findOne(entityId);
		delete(entity);
	}

	private Session getCurrentSession() {
		return DBHelper.getSession();
	}
}
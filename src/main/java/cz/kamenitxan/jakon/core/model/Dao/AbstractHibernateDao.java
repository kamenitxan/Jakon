package cz.kamenitxan.jakon.core.model.Dao;

import org.hibernate.Session;

import java.io.Serializable;
import java.util.List;

public class AbstractHibernateDao< T extends Serializable> {

	private Class< T > clazz;

	public AbstractHibernateDao(Class<T> clazz) {
		this.clazz = clazz;
	}

	public T findOne( long id ){
		return (T) getCurrentSession().get( clazz, id );
	}
	public List< T > findAll(){
		return getCurrentSession().createQuery( "from " + clazz.getName() ).list();
	}

	public void create( T entity ){
		getCurrentSession().persist( entity );
	}

	public void update( T entity ){
		getCurrentSession().merge( entity );
	}

	public void delete( T entity ){
		getCurrentSession().delete( entity );
	}
	public void deleteById( long entityId ) {
		T entity = findOne( entityId );
		delete( entity );
	}

	private Session getCurrentSession() {
		return DBHelper.getSession();
	}
}
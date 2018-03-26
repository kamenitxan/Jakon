package cz.kamenitxan.jakon.webui.controler.impl;

import cz.kamenitxan.jakon.core.model.Dao.DBHelper;
import cz.kamenitxan.jakon.core.model.JakonObject;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

public class Tets {
	public void test(){
		int pageNumber = 1;
		int pageSize = 10;
		Session session = DBHelper.getSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
		Root<JakonObject> root = countQuery.from(JakonObject.class);
		Expression<Long> expression = criteriaBuilder.count(root);
		countQuery.select(expression);
		Long count = session.createQuery(countQuery).getSingleResult();


		CriteriaQuery<JakonObject> criteriaQuery = criteriaBuilder.createQuery(JakonObject.class);
		Root<JakonObject> from = criteriaQuery.from(JakonObject.class);
		CriteriaQuery<JakonObject> select = criteriaQuery.select(from);
	}
}

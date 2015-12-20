package cz.kamenitxan.jakon.core.model.Dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import cz.kamenitxan.jakon.core.model.Category;
import cz.kamenitxan.jakon.core.model.Page;
import cz.kamenitxan.jakon.core.model.Post;

import java.sql.SQLException;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
 */
public class DaoHelper {
	private final String databaseUrl = "jdbc:sqlite:jakon.sqlite";
	private static Dao<Category, String> categoryDao = null;
	private static Dao<Page, Integer> pageDao = null;
	private static Dao<Post, Integer> postDao = null;

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			final ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
			categoryDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, Category.class);
			if (!categoryDao.isTableExists()) {
				TableUtils.createTable(connectionSource, Category.class);
			}
			pageDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, Page.class);
			if (!pageDao.isTableExists()) {
				TableUtils.createTable(connectionSource, Page.class);
			}
			postDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, Post.class);
			if (!postDao.isTableExists()) {
				TableUtils.createTable(connectionSource, Post.class);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Dao<Category, String> getCategoryDao() {
		return categoryDao;
	}

	public static Dao<Page, Integer> getPageDao() {
		return pageDao;
	}

	public static Dao<Post, Integer> getPostDao() {
		return postDao;
	}
}

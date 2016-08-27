package cz.kamenitxan.jakon.core.model.Dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.ReferenceObjectCache;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import cz.kamenitxan.jakon.core.Settings;
import cz.kamenitxan.jakon.core.model.Category;
import cz.kamenitxan.jakon.core.model.JakonObject;
import cz.kamenitxan.jakon.core.model.Page;
import cz.kamenitxan.jakon.core.model.Post;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
 */
public abstract class DBHelper {
	private static Map<Class<? extends JakonObject>, Dao<? extends JakonObject, Integer>> daos = new HashMap<>();

	static {
		try {
			Class.forName(Settings.getDatabaseDriver());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
			addDao(Post.class);
			addDao(Page.class);
			addDao(Category.class);
	}

	public static <T extends JakonObject> void addDao(Class<T> jobject) {
		try {
			final ConnectionSource connectionSource = new JdbcPooledConnectionSource(Settings.getDatabaseConnPath(), Settings.getProperty("databaseUser"), Settings.getProperty("databasePass"));

			Dao<T, Integer> dao = DaoManager.createDao(connectionSource, jobject);
			dao.setObjectCache(ReferenceObjectCache.makeSoftCache());
			if (!dao.isTableExists()) {
				TableUtils.createTable(connectionSource, jobject);
			}
			daos.put(jobject, dao);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Dao<? extends JakonObject, Integer> getDao(Class object) {
		return daos.get(object);
	}

	public static Dao<Post, Integer> getPostDao() {
		return (Dao<Post, Integer>) getDao(Post.class);
	}

	public static Dao<Page, Integer> getPageDao() {
		return (Dao<Page, Integer>) getDao(Page.class);
	}

	public static Dao<Category, Integer> getCategoryDao() {
		return (Dao<Category, Integer>) getDao(Category.class);
	}

	/**
	 * @param id searched JakonObject id
	 * @param refresh if true, object is queried from DB. not cache
	 * @return JakonObject or null
	 */
	@Deprecated
	public static JakonObject getObjectById(Integer id, boolean refresh) {
		try {
			for (Dao<? extends JakonObject, Integer> dao : daos.values()) {
				JakonObject o = dao.queryForId(id);
				if (o != null) {
					return o;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param id searched JakonObject id
	 * @return JakonObject or null
	 */
	public static JakonObject getObjectById(Integer id) {
		return getObjectById(id, false);
	}

}

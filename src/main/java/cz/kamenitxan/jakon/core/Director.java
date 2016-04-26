package cz.kamenitxan.jakon.core;

import com.j256.ormlite.dao.Dao;
import cz.kamenitxan.jakon.core.customPages.CustomPage;
import cz.kamenitxan.jakon.core.model.Dao.DBHelper;
import cz.kamenitxan.jakon.core.model.Post;
import cz.kamenitxan.jakon.core.template.Pebble;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Director<T extends CustomPage> {
	private Director instance = null;
	private List<T> customPages = new ArrayList<>();

	private Director() {
	}

	public void render() {
		try {
			Dao<Post, Integer> postDao = (Dao<Post, Integer>) DBHelper.getDao(Post.class);
			List<Post> posts = postDao.queryForAll();
			//new Pebble().render(posts.get(0).getTemplate(), posts);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void registerCustomPage(T page) {
		customPages.add(page);
	}

	public void removeCustomPage(T page) {
		customPages.remove(page);
	}

	public Director getInstance() {
		if (instance == null) {
			new Director();
		}
		return instance;
	}
}

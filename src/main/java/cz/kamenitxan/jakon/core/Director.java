package cz.kamenitxan.jakon.core;

import cz.kamenitxan.jakon.core.model.Category;
import cz.kamenitxan.jakon.core.model.Dao.DaoHelper;
import cz.kamenitxan.jakon.core.model.Page;
import cz.kamenitxan.jakon.core.model.Post;
import cz.kamenitxan.jakon.core.template.Pebble;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Director {

	public void render() {
		try {
			List<Post> posts = DaoHelper.getPostDao().queryForAll();
			new Pebble().render("list", posts);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

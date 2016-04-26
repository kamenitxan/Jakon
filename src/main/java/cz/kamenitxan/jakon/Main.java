package cz.kamenitxan.jakon;

import com.j256.ormlite.dao.Dao;
import cz.kamenitxan.jakon.core.Director;
import cz.kamenitxan.jakon.core.model.Dao.DBHelper;
import cz.kamenitxan.jakon.core.model.Post;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Main {
	public static void main(String[] args) {
		System.out.println("hi");

		Post post1 = new Post() {{
			setContent("lalala");
			setTemplate("post");
			setTitle("title");
			setUrl(getTitle());
		}};
		Dao dao = DBHelper.getDao(Post.class);


		new Director().render();
	}
}

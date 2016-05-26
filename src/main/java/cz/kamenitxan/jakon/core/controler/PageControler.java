package cz.kamenitxan.jakon.core.controler;

import cz.kamenitxan.jakon.core.model.Dao.DBHelper;
import cz.kamenitxan.jakon.core.model.Page;
import cz.kamenitxan.jakon.core.template.TemplateEngine;
import cz.kamenitxan.jakon.core.template.TemplateUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 01.05.16.
 */
public class PageControler implements IControler{
	private static String template = "page.peb";

	@Override
	public void generate() {
		TemplateEngine e = TemplateUtils.getEngine();
		try {
			List<Page> pages = DBHelper.getPageDao().queryForAll();
			for (Page p : pages) {
				Map<String, Object> context = new HashMap<>();
				context.put("page", p);
				e.render(template, p.getUrl() + p.getTitle(), context);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}

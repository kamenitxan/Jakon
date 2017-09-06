package cz.kamenitxan.jakon.example

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{Page, Post}
import cz.kamenitxan.jakon.example.pages.IndexPage

class JakonApp extends JakonInit{
	//DBHelper.addDao(classOf[Post])
	DBHelper.addDao(classOf[Page])

	Director.registerCustomPage(new IndexPage)


}

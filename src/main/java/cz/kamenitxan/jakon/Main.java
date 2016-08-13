package cz.kamenitxan.jakon;

import cz.kamenitxan.jakon.core.Director;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Main {
	final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		logger.info("Jakon started");
		Director.getInstance().init();
		logger.info("Jakon default init complete");
		Director.getInstance().render();
	}

}

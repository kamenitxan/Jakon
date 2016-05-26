package cz.kamenitxan.jakon;

import cz.kamenitxan.jakon.core.Director;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Main {
	public static void main(String[] args) {
		System.out.println("hi");

		Director.getInstance().init();
		Director.getInstance().render();
	}

}

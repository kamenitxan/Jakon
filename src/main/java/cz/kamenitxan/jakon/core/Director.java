package cz.kamenitxan.jakon.core;

import cz.kamenitxan.jakon.core.model.Page;
import cz.kamenitxan.jakon.core.template.Pebble;

import java.util.ArrayList;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Director {

	public void render() {
		new Pebble().render("list", new ArrayList<>());
	}

}

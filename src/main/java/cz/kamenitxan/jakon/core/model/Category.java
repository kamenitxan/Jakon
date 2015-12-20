package cz.kamenitxan.jakon.core.model;


import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
@Entity
public class Category {
	@Id
	String name;

	public Category() {
	}

	public String getName() {
		return name;
	}
}

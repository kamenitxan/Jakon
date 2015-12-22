package cz.kamenitxan.jakon.core.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
@Entity
public class Category {
	@Id
	@GeneratedValue
	private int id;
	@Column
	String name;

	public Category() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}
}

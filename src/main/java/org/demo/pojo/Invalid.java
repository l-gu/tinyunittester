package org.demo.pojo;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Standard POJO with default constructor + 1 setter and 1 getter for each attribute
 *
 */
public class Invalid {

	private int id ;
	private String name;
	private Float weight;
	private LocalDate birthDate ;
	private boolean manager;
	private UUID uid ;
	
	public Invalid() {
		super();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name + " foo"; // BUG simulation
	}
	public LocalDate getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}
	public boolean isManager() { // "is" getter
		return manager;
	}
	public void setManager(boolean manager) {
		this.manager = manager;
	}
	public Float getWeight() {
		return weight;
	}
	public void setWeight(Float weight) {
		this.weight = weight;
	}
	public UUID getUid() {
		return uid;
	}
	public void setUid(UUID uid) {
		this.uid = uid;
	}
	
	
}

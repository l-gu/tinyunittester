package org.demo.pojo;

import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

/**
 * @author laguerin
 *
 */
public class Imbalance {
	
	private final int id ;
	private String name;
	private Float weight;
	private LocalDate birthDate ;
	private boolean manager;
	private UUID uid ;
	
	public Imbalance() {
		super();
		this.id = new Random().nextInt();
	}
	
	// No setId(..)
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	// protected !
	protected LocalDate getBirthDate() { 
		return birthDate;
	}
	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}
	
	public boolean isManager() {
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

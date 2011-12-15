package model;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Embeddable;

@Embeddable
public class TeamMatchResult implements Serializable {
	private static final long serialVersionUID = 5493198563817908787L;
	private ArrayList<Integer> activeRobots;
	private ArrayList<Double> fluxDrain;
	/*
	private ArrayList<Integer> firepower;
	private ArrayList<Double> fluxIncome;
	private ArrayList<Double> fluxReserve;
	*/
	
	
	public ArrayList<Integer> getActiveRobots() {
		return activeRobots;
	}
	
	public ArrayList<Double> getFluxDrain() {
		return fluxDrain;
	}

	public void setFluxDrain(ArrayList<Double> fluxDrain) {
		this.fluxDrain = fluxDrain;
	}

	public void setActiveRobots(ArrayList<Integer> activeRobots) {
		this.activeRobots = activeRobots;
	}

}

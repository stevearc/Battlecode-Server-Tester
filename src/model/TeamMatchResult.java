package model;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Embeddable;

@Embeddable
public class TeamMatchResult implements Serializable {
	private static final long serialVersionUID = 5493198563817908787L;
	private ArrayList<Integer> activeRobots;
	private ArrayList<Double> fluxDrain;
	private ArrayList<Double> fluxIncome;
	private ArrayList<Double> fluxReserve;
	/*
	private ArrayList<Integer> firepower;
	*/
	
	
	public ArrayList<Integer> getActiveRobots() {
		return activeRobots;
	}
	
	public ArrayList<Double> getFluxDrain() {
		return fluxDrain;
	}

	public ArrayList<Double> getFluxIncome() {
		return fluxIncome;
	}

	public ArrayList<Double> getFluxReserve() {
		return fluxReserve;
	}

	public void setFluxIncome(ArrayList<Double> fluxIncome) {
		this.fluxIncome = fluxIncome;
	}

	public void setFluxReserve(ArrayList<Double> fluxReserve) {
		this.fluxReserve = fluxReserve;
	}

	public void setFluxDrain(ArrayList<Double> fluxDrain) {
		this.fluxDrain = fluxDrain;
	}

	public void setActiveRobots(ArrayList<Integer> activeRobots) {
		this.activeRobots = activeRobots;
	}

}

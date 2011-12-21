package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

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
	
	public static TeamMatchResult constructTeamMatchResult(long rounds) {
		TeamMatchResult tmr = new TeamMatchResult();
		Random r = new Random();
		ArrayList<Integer> activeRobots = new ArrayList<Integer>();
		ArrayList<Double> fluxDrain = new ArrayList<Double>();
		ArrayList<Double> fluxIncome = new ArrayList<Double>();
		ArrayList<Double> fluxReserve = new ArrayList<Double>();
		activeRobots.add(0);
		fluxDrain.add(1.0);
		fluxIncome.add(2.0);
		fluxReserve.add(40.0);
		for (int i = 1; i < rounds; i++) {
			double income = (double) Math.max(0, fluxIncome.get(i-1) + r.nextInt(3) - 1);
			int active = Math.max(0, activeRobots.get(i-1) + (fluxReserve.get(i-1) < 4 ? - 1 : r.nextInt(3) - 1));
			double drain = fluxDrain.get(i-1) + 1.5 * (active - activeRobots.get(i-1));
			double reserve = Math.max(0, income - drain);
			activeRobots.add(active);
			fluxIncome.add(income);
			fluxDrain.add(drain);
			fluxReserve.add(reserve);
		}
		tmr.setActiveRobots(activeRobots);
		tmr.setFluxIncome(fluxIncome);
		tmr.setFluxDrain(fluxDrain);
		tmr.setFluxReserve(fluxReserve);
		return tmr;
	}	
	
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

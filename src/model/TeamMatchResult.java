package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

@Embeddable
public class TeamMatchResult implements Serializable {
	private static final long serialVersionUID = 5493198563817908787L;
	private Integer[] activeRobots;
	private Double[] fluxDrain;
	private Double[] fluxIncome;
	private Double[] fluxReserve;
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
		tmr.setActiveRobots(activeRobots.toArray(new Integer[activeRobots.size()]));
		tmr.setFluxIncome(fluxIncome.toArray(new Double[fluxIncome.size()]));
		tmr.setFluxDrain(fluxDrain.toArray(new Double[fluxDrain.size()]));
		tmr.setFluxReserve(fluxReserve.toArray(new Double[fluxReserve.size()]));
		return tmr;
	}

	@Lob
	public Integer[] getActiveRobots() {
		return activeRobots;
	}

	@Lob
	public Double[] getFluxDrain() {
		return fluxDrain;
	}

	@Lob
	public Double[] getFluxIncome() {
		return fluxIncome;
	}

	@Lob
	public Double[] getFluxReserve() {
		return fluxReserve;
	}

	public void setActiveRobots(Integer[] activeRobots) {
		this.activeRobots = activeRobots;
	}

	public void setFluxDrain(Double[] fluxDrain) {
		this.fluxDrain = fluxDrain;
	}

	public void setFluxIncome(Double[] fluxIncome) {
		this.fluxIncome = fluxIncome;
	}

	public void setFluxReserve(Double[] fluxReserve) {
		this.fluxReserve = fluxReserve;
	}

}

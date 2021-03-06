package model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;

@Entity
public class TeamMatchResult implements Serializable {
	private static final long serialVersionUID = 5493198563817908787L;
	private Long id;
	private Integer[] totalRobots;
	private Integer[][] robotsByType;
	private Integer[] activeRobots;
	private Integer[][] activeRobotsByType;
	private Integer[] totalRobotsBuilt;
	private Integer[][] robotsBuiltByType;
	private Integer[] totalRobotsKilled;
	private Integer[][] robotsKilledByType;
	private Double[] fluxSpentOnSpawning;
	private Double[] fluxSpentOnMoving;
	private Double[] fluxSpentOnUpkeep;
	private Double[] totalFluxGathered;
	
	
	public static TeamMatchResult constructTeamMatchResult(long rounds) {
		// TODO: update this
		throw new RuntimeException("Not implemented yet for 2012");
		/*
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
		return tmr;
		*/
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO, generator="team_result_id_gen")
	@SequenceGenerator(name="team_result_id_gen", sequenceName="TEAM_RESULT_ID_GEN")
	public Long getId() {
		return id;
	}

	@Lob
	public Integer[] getTotalRobots() {
		return totalRobots;
	}

	@Lob
	public Integer[][] getRobotsByType() {
		return robotsByType;
	}

	@Lob
	public Integer[] getActiveRobots() {
		return activeRobots;
	}

	@Lob
	public Integer[][] getActiveRobotsByType() {
		return activeRobotsByType;
	}

	@Lob
	public Integer[] getTotalRobotsBuilt() {
		return totalRobotsBuilt;
	}

	@Lob
	public Integer[][] getRobotsBuiltByType() {
		return robotsBuiltByType;
	}

	@Lob
	public Integer[] getTotalRobotsKilled() {
		return totalRobotsKilled;
	}

	@Lob
	public Integer[][] getRobotsKilledByType() {
		return robotsKilledByType;
	}

	@Lob
	public Double[] getFluxSpentOnSpawning() {
		return fluxSpentOnSpawning;
	}

	@Lob
	public Double[] getFluxSpentOnMoving() {
		return fluxSpentOnMoving;
	}

	@Lob
	public Double[] getFluxSpentOnUpkeep() {
		return fluxSpentOnUpkeep;
	}

	@Lob
	public Double[] getTotalFluxGathered() {
		return totalFluxGathered;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTotalRobots(Integer[] totalRobots) {
		this.totalRobots = totalRobots;
	}


	public void setRobotsByType(Integer[][] robotsByType) {
		this.robotsByType = robotsByType;
	}


	public void setActiveRobots(Integer[] activeRobots) {
		this.activeRobots = activeRobots;
	}

	public void setActiveRobotsByType(Integer[][] activeRobotsByType) {
		this.activeRobotsByType = activeRobotsByType;
	}

	public void setTotalRobotsBuilt(Integer[] totalRobotsBuilt) {
		this.totalRobotsBuilt = totalRobotsBuilt;
	}


	public void setRobotsBuiltByType(Integer[][] robotsBuiltByType) {
		this.robotsBuiltByType = robotsBuiltByType;
	}


	public void setTotalRobotsKilled(Integer[] totalRobotsKilled) {
		this.totalRobotsKilled = totalRobotsKilled;
	}


	public void setRobotsKilledByType(Integer[][] robotsKilledByType) {
		this.robotsKilledByType = robotsKilledByType;
	}


	public void setFluxSpentOnSpawning(Double[] fluxSpentOnSpawning) {
		this.fluxSpentOnSpawning = fluxSpentOnSpawning;
	}


	public void setFluxSpentOnMoving(Double[] fluxSpentOnMoving) {
		this.fluxSpentOnMoving = fluxSpentOnMoving;
	}


	public void setFluxSpentOnUpkeep(Double[] fluxSpentOnUpkeep) {
		this.fluxSpentOnUpkeep = fluxSpentOnUpkeep;
	}
	
	
	public void setTotalFluxGathered(Double[] totalFluxGathered) {
		this.totalFluxGathered = totalFluxGathered;
	}

}

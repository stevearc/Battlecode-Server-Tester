package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import common.NetworkMatch;

@Entity
public class BSMatch {
	public static enum TEAM {TEAM_A, TEAM_B}
	public static enum WIN_CONDITION {DESTROY, POINTS}
	public static enum STATUS {QUEUED, RUNNING, FINISHED}
	private Long id;
	private BSRun run;
	private BSMap map;
	private Long seed;
	private STATUS status;
	private TEAM winner;
	private WIN_CONDITION winCondition;
	private Long rounds;
	private Double aPoints;
	private Double bPoints;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}
	@ManyToOne
	@JoinColumn(name = "bsrun_id")
	public BSRun getRun() {
		return run;
	}
	@ManyToOne
	@JoinColumn(name = "bsmap_id")
	public BSMap getMap() {
		return map;
	}
	@Column(nullable=false,updatable=false)
	public Long getSeed() {
		return seed;
	}
	@Enumerated(EnumType.STRING)
	public STATUS getStatus() {
		return status;
	}
	@Enumerated(EnumType.STRING)
	public TEAM getWinner() {
		return winner;
	}
	@Enumerated(EnumType.STRING)
	public WIN_CONDITION getWinCondition() {
		return winCondition;
	}
	public Long getRounds() {
		return rounds;
	}
	public Double getaPoints() {
		return aPoints;
	}
	public Double getbPoints() {
		return bPoints;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	public void setRun(BSRun run) {
		this.run = run;
	}
	public void setMap(BSMap map) {
		this.map = map;
	}
	public void setSeed(Long seed) {
		this.seed = seed;
	}
	public void setStatus(STATUS status) {
		this.status = status;
	}
	public void setWinner(TEAM winner) {
		this.winner = winner;
	}
	public void setWinCondition(WIN_CONDITION winCondition) {
		this.winCondition = winCondition;
	}
	public void setRounds(Long rounds) {
		this.rounds = rounds;
	}
	public void setaPoints(Double aPoints) {
		this.aPoints = aPoints;
	}
	public void setbPoints(Double bPoints) {
		this.bPoints = bPoints;
	}
	
	public NetworkMatch buildNetworkMatch() {
		return new NetworkMatch(run.getId(), id, run.getTeamA().getPlayerName(), run.getTeamB().getPlayerName(), map, seed);
	}

}

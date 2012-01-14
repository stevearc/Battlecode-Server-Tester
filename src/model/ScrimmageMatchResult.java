package model;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class ScrimmageMatchResult implements MatchResult, Serializable{
	private static final long serialVersionUID = 386380213941385025L;
	private Long id;
	private BSScrimmageSet scrimmageSet;
	private TEAM winner;
	private WIN_CONDITION winCondition;
	private Long rounds;
	private TeamMatchResult aResult;
	private TeamMatchResult bResult;
	private String map;
	
	public ScrimmageMatchResult() {
		
	}
	
	public ScrimmageMatchResult(MatchResultImpl mr) {
		winner = mr.getWinner();
		winCondition = mr.getWinCondition();
		rounds = mr.getRounds();
		aResult = mr.getaResult();
		bResult = mr.getbResult();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO, generator="scrim_result_id_gen")
	@SequenceGenerator(name="scrim_result_id_gen", sequenceName="SCRIM_RESULT_ID_GEN")
	public Long getId() {
		return id;
	}
	@ManyToOne
	@JoinColumn(name = "bsrun_id")
	public BSScrimmageSet getScrimmageSet() {
		return scrimmageSet;
	}
	@Embedded
	@AttributeOverrides( {
		@AttributeOverride(name="totalRobots", column = @Column(name="totalRobots_A")),
		@AttributeOverride(name="robotsByType", column = @Column(name="robotsByType_A")),
		@AttributeOverride(name="activeRobots", column = @Column(name="activeRobots_A")),
		@AttributeOverride(name="activeRobotsByType", column = @Column(name="activeRobotsByType_A")),
		@AttributeOverride(name="totalRobotsBuilt", column = @Column(name="totalRobotsBuilt_A")),
		@AttributeOverride(name="robotsBuiltByType", column = @Column(name="robotsBuiltByType_A")),
		@AttributeOverride(name="totalRobotsKilled", column = @Column(name="totalRobotsKilled_A")),
		@AttributeOverride(name="robotsKilledByType", column = @Column(name="robotsKilledByType_A")),
		@AttributeOverride(name="fluxSpentOnSpawning", column = @Column(name="fluxSpentOnSpawning_A")),
		@AttributeOverride(name="fluxSpentOnMoving", column = @Column(name="fluxSpentOnMoving_A")),
		@AttributeOverride(name="fluxSpentOnUpkeep", column = @Column(name="fluxSpentOnUpkeep_A")),
		@AttributeOverride(name="totalFluxGathered", column = @Column(name="totalFluxGathered_A"))
	})
	public TeamMatchResult getaResult() {
		return aResult;
	}
	@Embedded
	@AttributeOverrides( {
		@AttributeOverride(name="totalRobots", column = @Column(name="totalRobots_B")),
		@AttributeOverride(name="robotsByType", column = @Column(name="robotsByType_B")),
		@AttributeOverride(name="activeRobots", column = @Column(name="activeRobots_B")),
		@AttributeOverride(name="activeRobotsByType", column = @Column(name="activeRobotsByType_B")),
		@AttributeOverride(name="totalRobotsBuilt", column = @Column(name="totalRobotsBuilt_B")),
		@AttributeOverride(name="robotsBuiltByType", column = @Column(name="robotsBuiltByType_B")),
		@AttributeOverride(name="totalRobotsKilled", column = @Column(name="totalRobotsKilled_B")),
		@AttributeOverride(name="robotsKilledByType", column = @Column(name="robotsKilledByType_B")),
		@AttributeOverride(name="fluxSpentOnSpawning", column = @Column(name="fluxSpentOnSpawning_B")),
		@AttributeOverride(name="fluxSpentOnMoving", column = @Column(name="fluxSpentOnMoving_B")),
		@AttributeOverride(name="fluxSpentOnUpkeep", column = @Column(name="fluxSpentOnUpkeep_B")),
		@AttributeOverride(name="totalFluxGathered", column = @Column(name="totalFluxGathered_B"))
	})
	public TeamMatchResult getbResult() {
		return bResult;
	}
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
	@Column(nullable=false,updatable=false)
	public String getMap() {
		return map;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	public void setScrimmageSet(BSScrimmageSet scrimmageSet) {
		this.scrimmageSet = scrimmageSet;
	}
	public void setaResult(TeamMatchResult aResult) {
		this.aResult = aResult;
	}
	public void setbResult(TeamMatchResult bResult) {
		this.bResult = bResult;
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
	

	public void setMap(String map) {
		this.map = map;
	}

}

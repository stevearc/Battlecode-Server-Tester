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
import javax.persistence.SequenceGenerator;

@Entity
public class MatchResult implements Serializable {
	private static final long serialVersionUID = 8212825736833239951L;
	public static enum WIN_CONDITION {DESTROY, POINTS}
	private Long id;
	private TEAM winner;
	private WIN_CONDITION winCondition;
	private Long rounds;
	private TeamMatchResult aResult;
	private TeamMatchResult bResult;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO, generator="match_result_id_gen")
	@SequenceGenerator(name="match_result_id_gen", sequenceName="MATCH_RESULT_ID_GEN")
	public Long getId() {
		return id;
	}
	@Embedded
	@AttributeOverrides( {
		@AttributeOverride(name="activeRobots", column = @Column(name="activeRobots_A")),
		@AttributeOverride(name="fluxDrain", column = @Column(name="fluxDrain_A"))
	})
	public TeamMatchResult getaResult() {
		return aResult;
	}
	@Embedded
	@AttributeOverrides( {
		@AttributeOverride(name="activeRobots", column = @Column(name="activeRobots_B")),
		@AttributeOverride(name="fluxDrain", column = @Column(name="fluxDrain_B"))
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
	
	public void setId(Long id) {
		this.id = id;
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

}

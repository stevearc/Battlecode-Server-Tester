package model;

import java.io.Serializable;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

@Entity
public class MatchResultImpl implements MatchResult, Serializable {
	private static final long serialVersionUID = 8212825736833239951L;
	private Long id;
	private TEAM winner;
	private WIN_CONDITION winCondition;
	private Long rounds;
	private TeamMatchResult aResult;
	private TeamMatchResult bResult;
	
	public static MatchResultImpl constructMockMatchResult() {
		MatchResultImpl result = new MatchResultImpl();
		Random r = new Random();
		result.setWinCondition(WIN_CONDITION.values()[r.nextInt(WIN_CONDITION.values().length)]);
		result.setWinner(TEAM.values()[r.nextInt(TEAM.values().length)]);
		result.setRounds(900 + new Long(r.nextInt(201)));
		result.setaResult(TeamMatchResult.constructTeamMatchResult(result.getRounds()));
		result.setbResult(TeamMatchResult.constructTeamMatchResult(result.getRounds()));
		return result;
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO, generator="match_result_id_gen")
	@SequenceGenerator(name="match_result_id_gen", sequenceName="MATCH_RESULT_ID_GEN")
	public Long getId() {
		return id;
	}
	
	@OneToOne(orphanRemoval=true)
	@LazyToOne(LazyToOneOption.NO_PROXY)
	public TeamMatchResult getaResult() {
		return aResult;
	}
	
	@OneToOne(orphanRemoval=true)
	@LazyToOne(LazyToOneOption.NO_PROXY)
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

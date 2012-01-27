package model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

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
	private String observations;
	
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
	@Column(nullable=false,updatable=false)
	public String getMap() {
		return map;
	}
	@Transient
	public String getObservations() {
		return observations;
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
	public void setObservations(String observations) {
		this.observations = observations;
	}
	

	public void setMap(String map) {
		this.map = map;
	}

}

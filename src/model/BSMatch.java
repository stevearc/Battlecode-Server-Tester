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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import common.NetworkMatch;

@Entity
public class BSMatch {
	private Long id;
	private BSRun run;
	private BSMap map;
	private Long seed;
	private STATUS status;
	private MatchResultImpl result;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO, generator="match_id_gen")
	@SequenceGenerator(name="match_id_gen", sequenceName="MATCH_ID_GEN")
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
	@OneToOne
	public MatchResultImpl getResult() {
		return result;
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
	public void setResult(MatchResultImpl results) {
		this.result = results;
	}
	
	public NetworkMatch buildNetworkMatch() {
		return new NetworkMatch(run.getId(), id, run.getTeamA().getPlayerName(), run.getTeamB().getPlayerName(), map, seed);
	}
	
	public String toMatchFileName() {
		return getRun().getId() + getMap().getMapName() + getSeed() + ".rms";
	}
	
	public String toOutputFileName() {
		return getRun().getId() + getMap().getMapName() + getSeed() + ".out";
	}
	
	public String toObsFileName() {
		return getRun().getId() + getMap().getMapName() + getSeed() + "-obs.out";
	}

}

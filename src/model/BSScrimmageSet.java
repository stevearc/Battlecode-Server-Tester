package model;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

/**
 * This represents the set of 2-3 matches that are in a scrimmage
 * @author stevearc
 *
 */
@Entity
public class BSScrimmageSet implements Serializable{
	private static final long serialVersionUID = 5227234406228940400L;

	private Long id;
	private String fileName;
	private STATUS status;
	private String playerA;
	private String playerB;
	private TEAM winner;
	private List<ScrimmageMatchResult> scrimmageMatches;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO, generator="scrimmage_id_gen")
	@SequenceGenerator(name="scrimmage_id_gen", sequenceName="SCRIMMAGE_ID_GEN")
	public Long getId() {
		return id;
	}
	
	@Column(nullable=false,updatable=false)
	public String getFileName() {
		return fileName;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	public STATUS getStatus() {
		return status;
	}

	public String getPlayerA() {
		return playerA;
	}

	public String getPlayerB() {
		return playerB;
	}

	@Enumerated(EnumType.STRING)
	public TEAM getWinner() {
		return winner;
	}

	@OneToMany(mappedBy="scrimmageSet", fetch=FetchType.LAZY, orphanRemoval=true)
	public List<ScrimmageMatchResult> getScrimmageMatches() {
		return scrimmageMatches;
	}

	public String toPath() {
		return "static" + File.separator + "scrimmages" + File.separator + fileName;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setStatus(STATUS status) {
		this.status = status;
	}

	public void setPlayerA(String playerA) {
		this.playerA = playerA;
	}

	public void setPlayerB(String playerB) {
		this.playerB = playerB;
	}

	public void setWinner(TEAM winner) {
		this.winner = winner;
	}

	public void setScrimmageMatches(List<ScrimmageMatchResult> scrimmageMatches) {
		this.scrimmageMatches = scrimmageMatches;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BSScrimmageSet))
			return false;
		BSScrimmageSet other = (BSScrimmageSet) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}

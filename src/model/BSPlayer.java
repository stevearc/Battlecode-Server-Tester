package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BSPlayer {
	private Long id;
	private String playerName;
	private Boolean invisible;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}

	@Column(updatable=false, nullable=false, length=50, unique=true)
	public String getPlayerName() {
		return playerName;
	}
	
	@Column(nullable=false, columnDefinition="boolean default false")
	public Boolean getInvisible() {
		return invisible;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public void setInvisible(Boolean invisible) {
		this.invisible = invisible;
	}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BSPlayer) {
			BSPlayer p = (BSPlayer) o;
			return p.id.equals(id);
		}
		return false;
	}
}

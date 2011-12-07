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

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}

	@Column(updatable=false, nullable=false, length=50, unique=true)
	public String getPlayerName() {
		return playerName;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
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

package common;

import java.io.Serializable;

public class Dependencies implements Serializable {
	private static final long serialVersionUID = -2280546354744824747L;
	
	public final byte[] map;
	public final byte[] teamA;
	public final byte[] teamB;
	public final String mapName;
	public final String teamAName;
	public final String teamBName;
	
	public Dependencies(String mapName, byte[] map, String teamAName, byte[] teamA, String teamBName, byte[] teamB) {
		this.mapName = mapName;
		this.map = map;
		this.teamAName = teamAName;
		this.teamA = teamA;
		this.teamBName = teamBName;
		this.teamB = teamB;
	}
	
	@Override
	public String toString() {
		return "Dependency: " + (map == null ? "" : mapName + ".xml ") + 
		(teamA == null ? "" : teamAName + ".jar ") + 
		(teamB == null ? "" : teamBName + ".jar ");
	}

}

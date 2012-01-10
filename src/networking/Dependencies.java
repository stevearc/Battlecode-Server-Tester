package networking;

import java.io.Serializable;

public class Dependencies implements Serializable {
	private static final long serialVersionUID = -2280546354744824747L;
	
	public final byte[] map;
	public final byte[] teamA;
	public final byte[] teamB;
	public final String mapName;
	public final String teamAName;
	public final String teamBName;
	public final byte[] battlecodeServer;
	public final byte[] allowedPackages;
	public final byte[] disallowedClasses;
	public final byte[] methodCosts;
	
	public Dependencies(byte[] battlecodeServer, byte[] allowedPackages, byte[] disallowedClasses, 
			byte[] methodCosts, String mapName, byte[] map, String teamAName, byte[] teamA, 
			String teamBName, byte[] teamB) {
		this.battlecodeServer = battlecodeServer;
		this.allowedPackages = allowedPackages;
		this.disallowedClasses = disallowedClasses;
		this.methodCosts = methodCosts;
		this.mapName = mapName;
		this.map = map;
		this.teamAName = teamAName;
		this.teamA = teamA;
		this.teamBName = teamBName;
		this.teamB = teamB;
	}
	
	@Override
	public String toString() {
		return "Dependency: " + (battlecodeServer == null ? "" : "battlecode-server.jar ") + 
		(allowedPackages == null ? "" : "AllowedPackages.txt ") + 
		(disallowedClasses == null ? "" : "DisallowedClasses.txt ") + 
		(methodCosts == null ? "" : "MethodCosts.txt ") + 
		(map == null ? "" : mapName + ".xml ") + 
		(teamA == null ? "" : teamAName + ".jar ") + 
		(teamB == null ? "" : teamBName + ".jar ");
	}

}

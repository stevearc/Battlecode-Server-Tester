package common;

import java.io.Serializable;

/**
 * Wrapper for Match information to be sent over network between Server and Worker
 * @author stevearc
 *
 */
public class Match implements Serializable {
	private static final long serialVersionUID = 6673793068304545925L;
	public int run_id;
	public int id;
	public String team_a;
	public String team_b;
	public BattlecodeMap map;
	public int seed;
	
	public Match(int run_id, int id, String team_a, String team_b, BattlecodeMap map, int seed) {
		this.run_id = run_id;
		this.id = id;
		this.team_a = team_a;
		this.team_b = team_b;
		this.map = map;
		this.seed = seed;
	}

	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Match) {
			Match m = (Match) o;
			return m.id == id;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return map + " (" + seed + "): " + team_a + " vs. " + team_b;
	}
	
	public String toMapString() {
		return map + " (" + seed + ")";
	}
}

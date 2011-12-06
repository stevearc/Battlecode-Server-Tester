package common;

import java.io.Serializable;

import model.BSMap;



/**
 * Wrapper for Match information to be sent over network between Server and Worker
 * @author stevearc
 *
 */
public class NetworkMatch implements Serializable {
	private static final long serialVersionUID = 6673793068304545925L;
	public Long run_id;
	public Long id;
	public String team_a;
	public String team_b;
	public BSMap map;
	public Long seed;
	
	public NetworkMatch(Long run_id, Long id, String team_a, String team_b, BSMap map, Long seed) {
		this.run_id = run_id;
		this.id = id;
		this.team_a = team_a;
		this.team_b = team_b;
		this.map = map;
		this.seed = seed;
	}

	// TODO: remove references to hashCode and equals
	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof NetworkMatch) {
			NetworkMatch m = (NetworkMatch) o;
			return id.equals(m.id);
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

package common;

import java.io.Serializable;


public class Match implements Serializable {
	private static final long serialVersionUID = 6673793068304545925L;
	public int run_id;
	public String team_a;
	public String team_b;
	public String map;
	
	public Match(int run_id, String team_a, String team_b, String map) {
		this.run_id = run_id;
		this.team_a = team_a;
		this.team_b = team_b;
		this.map = map;
	}

	@Override
	public int hashCode() {
		return team_a.hashCode() + team_b.hashCode() + map.hashCode() + run_id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Match) {
			Match m = (Match) o;
			return m.team_a.equals(team_a) && m.team_b.equals(team_b) && m.map.equals(map) && m.run_id == run_id;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return run_id + " " + map + ": " + team_a + " vs. " + team_b;
	}
}

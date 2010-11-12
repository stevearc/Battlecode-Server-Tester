package common;

import java.io.Serializable;


public class Match implements Serializable {
	private static final long serialVersionUID = 6673793068304545925L;
	public int run_id;
	public String team_a;
	public String team_b;
	public BattlecodeMap map;
	public int reverse;
	
	public Match(int run_id, String team_a, String team_b, BattlecodeMap map, int reverse) {
		this.run_id = run_id;
		this.team_a = team_a;
		this.team_b = team_b;
		this.map = map;
		this.reverse = reverse;
	}

	@Override
	public int hashCode() {
		return team_a.hashCode() + team_b.hashCode() + map.hashCode() + run_id + reverse;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Match) {
			Match m = (Match) o;
			return m.team_a.equals(team_a) && m.team_b.equals(team_b) && m.map.equals(map) && m.run_id == run_id && m.reverse == reverse;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return run_id + " " + map + ": " + (reverse == 0 ? team_a : team_b) + " vs. " + (reverse == 0 ? team_b : team_a);
	}
}

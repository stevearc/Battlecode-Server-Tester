package common;

public class MatchSet {
	private String team_a;
	private String team_b;
	
	public MatchSet(String team_a, String team_b) {
		this.team_a = team_a;
		this.team_b = team_b;
	}

	public String getTeam_a() {
		return team_a;
	}

	public String getTeam_b() {
		return team_b;
	}

	@Override
	public int hashCode() {
		return team_a.hashCode() + team_b.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof MatchSet) {
			MatchSet ms = (MatchSet) o;
			return ms.team_a.equals(team_a) && ms.team_b.equals(team_b);
		}
		return false;
	}
}

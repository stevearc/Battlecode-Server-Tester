package model;


public interface MatchResult {
	public static enum WIN_CONDITION {DESTROY, POINTS, ENERGON}
	
	public Long getId();
	public TeamMatchResult getaResult();
	public TeamMatchResult getbResult();
	public TEAM getWinner();
	public WIN_CONDITION getWinCondition();
	public Long getRounds();
	
	public void setId(Long id);
	public void setaResult(TeamMatchResult aResult);
	public void setbResult(TeamMatchResult bResult);
	public void setWinner(TEAM winner);
	public void setWinCondition(WIN_CONDITION winCondition);
	public void setRounds(Long rounds);
}

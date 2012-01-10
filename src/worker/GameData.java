package worker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import model.MatchResultImpl;
import model.TEAM;
import model.TeamMatchResult;
import model.MatchResult.WIN_CONDITION;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.serial.ExtensibleMetadata;
import battlecode.serial.GameStats;
import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;
import battlecode.server.proxy.Proxy;
import battlecode.world.signal.BytecodesUsedSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.FluxChangeSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.SpawnSignal;

/**
 * This class reads and analyzes rms (match) files.  It links against battlecode-server.jar
 * @author stevearc
 *
 */
public class GameData extends Proxy {
	// Maps robotID to info
	private HashMap<Integer, RobotStat> robots = new HashMap<Integer, RobotStat>();
	private HashMap<Integer, RobotStat>[] archons;

	private MatchFooter footer;
	private ArrayList<RoundDelta> rounds = new ArrayList<RoundDelta>();
	private GameStats gameStats;
	private String teamA;
	private String teamB;
	private String[] maps;

	@Override
	public void open() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return null;
	}

	public void addData(Object o) throws IOException {
		if (o instanceof RoundDelta) {
			writeRound((RoundDelta) o);
		} else if (o instanceof MatchFooter) {
			writeFooter((MatchFooter) o);
		} else if (o instanceof RoundStats) {
			writeStats((RoundStats) o);
		} else {
			writeObject(o);
		}
	}

	@Override
	public void writeObject(Object o) {
		if (o instanceof GameStats) {
			gameStats = (GameStats) o;
		} else if (o instanceof ExtensibleMetadata) {
			ExtensibleMetadata em = (ExtensibleMetadata)o;
			if (em.get("type", "").equals("header")) {
				teamA = (String)em.get("team-a", "");
				teamB = (String)em.get("team-b", "");
				maps = (String[])em.get("maps", "");
			}
		}
	}

	@Override
	public void writeHeader(MatchHeader header) throws IOException {

	}

	@Override
	public void writeRound(RoundDelta round) throws IOException {
		this.rounds.add(round);
	}

	@Override
	public void writeFooter(MatchFooter footer) throws IOException {
		this.footer = footer;
	}

	@Override
	public void writeStats(RoundStats stats) throws IOException {

	}

	public String getTeamA() {
		return teamA;
	}

	public String getTeamB() {
		return teamB;
	}

	public String[] getMaps() {
		return maps;
	}

	@SuppressWarnings("unchecked")
	public MatchResultImpl analyzeMatch() {
		MatchResultImpl matchResult = new MatchResultImpl();
		RobotStat r;
		Team[] teams = Team.values();
		archons = new HashMap[teams.length];
		TeamMatchResult[] teamResults = new TeamMatchResult[teams.length];
		ArrayList<Integer>[] totalRobots = new ArrayList[teams.length];
		Integer[] currentRobots = new Integer[teams.length];
		ArrayList<Integer>[][] robotsByType = new ArrayList[teams.length][RobotType.values().length];
		Integer[][] currentRobotsByType = new Integer[teams.length][RobotType.values().length];
		ArrayList<Integer>[] activeRobots = new ArrayList[teams.length];
		Integer[] currentActiveRobots = new Integer[teams.length];
		ArrayList<Integer>[][] activeRobotsByType = new ArrayList[teams.length][RobotType.values().length];
		Integer[][] currentActiveRobotsByType = new Integer[teams.length][RobotType.values().length];
		ArrayList<Double>[] fluxSpentOnSpawning = new ArrayList[teams.length];
		Double[] currentFluxSpentOnSpawning = new Double[teams.length];
		ArrayList<Double>[] fluxSpentOnMoving = new ArrayList[teams.length];
		Double[] currentFluxSpentOnMoving = new Double[teams.length];
		ArrayList<Double>[] fluxSpentOnUpkeep = new ArrayList[teams.length];
		Double[] currentFluxSpentOnUpkeep = new Double[teams.length];
		ArrayList<Integer>[] robotsBuilt = new ArrayList[teams.length];
		Integer[] currentRobotsBuilt = new Integer[teams.length];
		ArrayList<Integer>[][] robotsBuiltByType = new ArrayList[teams.length][RobotType.values().length];
		Integer[][] currentRobotsBuiltByType = new Integer[teams.length][RobotType.values().length];
		ArrayList<Integer>[] robotsKilled = new ArrayList[teams.length];
		Integer[] currentRobotsKilled = new Integer[teams.length];
		ArrayList<Integer>[][] robotsKilledByType = new ArrayList[teams.length][RobotType.values().length];
		Integer[][] currentRobotsKilledByType = new Integer[teams.length][RobotType.values().length];
		ArrayList<Double>[] fluxGathered = new ArrayList[teams.length];
		Double[] currentFluxGathered = new Double[teams.length];

		for (int i = 0; i < teams.length; i++) {
			archons[i] = new HashMap<Integer, RobotStat>();
			teamResults[i] = new TeamMatchResult();
			totalRobots[i] = new ArrayList<Integer>();
			currentRobots[i] = 0;
			activeRobots[i] = new ArrayList<Integer>();
			currentActiveRobots[i] = 0;
			fluxSpentOnSpawning[i] = new ArrayList<Double>();
			currentFluxSpentOnSpawning[i] = 0.0;
			fluxSpentOnMoving[i] = new ArrayList<Double>();
			currentFluxSpentOnMoving[i] = 0.0;
			fluxSpentOnUpkeep[i] = new ArrayList<Double>();
			currentFluxSpentOnUpkeep[i] = 0.0;
			robotsBuilt[i] = new ArrayList<Integer>();
			currentRobotsBuilt[i] = 0;
			robotsKilled[i] = new ArrayList<Integer>();
			currentRobotsKilled[i] = 0;
			fluxGathered[i] = new ArrayList<Double>();
			currentFluxGathered[i] = 0.0;
			for (int j = 0; j < RobotType.values().length; j++) {
				robotsByType[i][j] = new ArrayList<Integer>();
				currentRobotsByType[i][j] = 0;
				activeRobotsByType[i][j] = new ArrayList<Integer>();
				currentActiveRobotsByType[i][j] = 0;
				robotsBuiltByType[i][j] = new ArrayList<Integer>();
				currentRobotsBuiltByType[i][j] = 0;
				robotsKilledByType[i][j] = new ArrayList<Integer>();
				currentRobotsKilledByType[i][j] = 0;
			}
		}

		RoundDelta round;
		for (int l = 0; l < rounds.size(); l++) {
			round = rounds.get(l);

			Signal[] signals = round.getSignals();
			for (Signal signal: signals) {
				if (signal instanceof SpawnSignal) 
				{
					SpawnSignal s = (SpawnSignal)signal;
					currentRobots[s.getTeam().ordinal()]++;
					currentRobotsByType[s.getTeam().ordinal()][s.getType().ordinal()]++;
					r = new RobotStat(s.getTeam(), s.getType());
					r.setLocation(s.getLoc());
					if (r.recalculateOnStatus()) {
						if (r.isOn()) {
							currentActiveRobots[s.getTeam().ordinal()]++;
							currentActiveRobotsByType[s.getTeam().ordinal()][s.getType().ordinal()]++;
						}
					}
					currentFluxSpentOnSpawning[s.getTeam().ordinal()] += s.getType().spawnCost;
					currentRobotsBuilt[s.getTeam().ordinal()]++;
					currentRobotsBuiltByType[s.getTeam().ordinal()][s.getType().ordinal()]++;
					robots.put(s.getRobotID(), r);
					if (s.getType() == RobotType.ARCHON) {
						archons[r.team.ordinal()].put(s.getRobotID(), r);
					}
				} 
				else if(signal instanceof DeathSignal) 
				{
					DeathSignal s = (DeathSignal)signal;
					r = robots.remove(s.getObjectID());
					if (r != null) {
						currentRobots[r.team.ordinal()]--;
						currentRobotsByType[r.team.ordinal()][r.type.ordinal()]--;
						if (r.isOn()) {
							currentActiveRobots[r.team.ordinal()]--;
							currentActiveRobotsByType[r.team.ordinal()][r.type.ordinal()]--;
						}
						currentRobotsKilled[r.team.opponent().ordinal()]++;
						currentRobotsKilledByType[r.team.opponent().ordinal()][r.type.ordinal()]++;
						if (r.type == RobotType.ARCHON) {
							archons[r.team.ordinal()].remove(s.getObjectID());
						}
					}
				} 
				else if (signal instanceof MovementSignal) 
				{
					MovementSignal s = (MovementSignal)signal;
					r = robots.get(s.getRobotID());
					currentFluxSpentOnMoving[r.team.ordinal()] += r.type.moveCost;
					r.setLocation(s.getNewLoc());
				} 
				else if (signal instanceof FluxChangeSignal) 
				{
					FluxChangeSignal s = (FluxChangeSignal)signal;
					for (int i = 0; i < s.getRobotIDs().length; i++) {
						r = robots.get(s.getRobotIDs()[i]);
						r.setFlux(s.getFlux()[i]);
						if (r.type != RobotType.ARCHON && r.type != RobotType.TOWER) {
							currentFluxSpentOnUpkeep[r.team.ordinal()] += GameConstants.UNIT_UPKEEP;
						}
						if (r.recalculateOnStatus()) {
							if (r.isOn()) {
								currentActiveRobots[r.team.ordinal()]++;
								currentActiveRobotsByType[r.team.ordinal()][r.type.ordinal()]++;
							} else {
								currentActiveRobots[r.team.ordinal()]--;
								currentActiveRobotsByType[r.team.ordinal()][r.type.ordinal()]--;
							}
						}
					}
				} 
				else if (signal instanceof BytecodesUsedSignal) 
				{
					BytecodesUsedSignal s = (BytecodesUsedSignal)signal;
					for (int i = 0; i < s.getRobotIDs().length; i++) {
						r = robots.get(s.getRobotIDs()[i]);
						int bytecodesBelowBase = GameConstants.BYTECODE_LIMIT - s.getNumBytecodes()[i];
						if(bytecodesBelowBase > 0 && r.type != RobotType.ARCHON)
							currentFluxSpentOnUpkeep[r.team.ordinal()] += 
								(GameConstants.YIELD_BONUS*bytecodesBelowBase/GameConstants.BYTECODE_LIMIT*GameConstants.UNIT_UPKEEP);		
					}
				}
			}
			// Calculate the flux produced this round
			// This algorithm is copy/pasted from InternalRobot
			for (int i = 0; i < teams.length; i++) {
				for (RobotStat rs: archons[i].values()) {
					int d, dmin = GameConstants.PRODUCTION_PENALTY_R2;
					for (RobotStat other: archons[i].values()) {
						d = rs.getLocation().distanceSquaredTo(other.getLocation());
						if(d>0&&d<=dmin)
							dmin=d;
					}
					double prod = GameConstants.MIN_PRODUCTION + (GameConstants.MAX_PRODUCTION - GameConstants.MIN_PRODUCTION)*
					Math.sqrt(((double)dmin)/GameConstants.PRODUCTION_PENALTY_R2);
					currentFluxGathered[i] += prod;
				}
			}

			for (int i = 0; i < teams.length; i++) {
				totalRobots[i].add(currentRobots[i]);
				activeRobots[i].add(currentActiveRobots[i]);
				fluxSpentOnSpawning[i].add(currentFluxSpentOnSpawning[i]);
				fluxSpentOnMoving[i].add(currentFluxSpentOnMoving[i]);
				fluxSpentOnUpkeep[i].add(currentFluxSpentOnUpkeep[i]);
				robotsBuilt[i].add(currentRobotsBuilt[i]);
				robotsKilled[i].add(currentRobotsKilled[i]);
				fluxGathered[i].add(currentFluxGathered[i]);
				for (int j = 0; j < RobotType.values().length; j++) {
					robotsByType[i][j].add(currentRobotsByType[i][j]);
					activeRobotsByType[i][j].add(currentActiveRobotsByType[i][j]);
					robotsBuiltByType[i][j].add(currentRobotsBuiltByType[i][j]);
					robotsKilledByType[i][j].add(currentRobotsKilledByType[i][j]);
				}
			}
		}
		for (int i = 0; i < teams.length; i++) {
			teamResults[i].setTotalRobots(totalRobots[i].toArray(new Integer[rounds.size()]));
			teamResults[i].setActiveRobots(activeRobots[i].toArray(new Integer[rounds.size()]));
			teamResults[i].setFluxSpentOnSpawning(fluxSpentOnSpawning[i].toArray(new Double[rounds.size()]));
			teamResults[i].setFluxSpentOnMoving(fluxSpentOnMoving[i].toArray(new Double[rounds.size()]));
			teamResults[i].setFluxSpentOnUpkeep(fluxSpentOnUpkeep[i].toArray(new Double[rounds.size()]));
			teamResults[i].setTotalRobotsBuilt(robotsBuilt[i].toArray(new Integer[rounds.size()]));
			teamResults[i].setTotalRobotsKilled(robotsKilled[i].toArray(new Integer[rounds.size()]));
			teamResults[i].setTotalFluxGathered(fluxGathered[i].toArray(new Double[rounds.size()]));

			Integer[][] robotsByTypeArray = new Integer[RobotType.values().length][];
			Integer[][] activeRobotsByTypeArray = new Integer[RobotType.values().length][];
			Integer[][] robotsBuiltByTypeArray = new Integer[RobotType.values().length][];
			Integer[][] robotsKilledByTypeArray = new Integer[RobotType.values().length][];
			for (int j = 0; j < RobotType.values().length; j++) {
				robotsByTypeArray[j] = robotsByType[i][j].toArray(new Integer[rounds.size()]);
				activeRobotsByTypeArray[j] = activeRobotsByType[i][j].toArray(new Integer[rounds.size()]);
				robotsBuiltByTypeArray[j] = robotsBuiltByType[i][j].toArray(new Integer[rounds.size()]);
				robotsKilledByTypeArray[j] = robotsKilledByType[i][j].toArray(new Integer[rounds.size()]);
			}
			teamResults[i].setRobotsByType(robotsByTypeArray);
			teamResults[i].setActiveRobotsByType(activeRobotsByTypeArray);
			teamResults[i].setRobotsBuiltByType(robotsBuiltByTypeArray);
			teamResults[i].setRobotsKilledByType(robotsKilledByTypeArray);

		}

		matchResult.setRounds(new Long(rounds.size()));
		matchResult.setWinner(convertTeam(footer.getWinner()));
		switch (gameStats.getDominationFactor()) {
		case DESTROYED:
			matchResult.setWinCondition(WIN_CONDITION.DESTROY);
			break;
		case OWNED:
			matchResult.setWinCondition(WIN_CONDITION.ARCHONS);
			break;
		default:
			matchResult.setWinCondition(WIN_CONDITION.ENERGON);
			break;
		}
		matchResult.setaResult(teamResults[Team.A.ordinal()]);
		matchResult.setbResult(teamResults[Team.B.ordinal()]);
		return matchResult;
	}

	private TEAM convertTeam(Team battlecodeTeam) {
		switch (battlecodeTeam) {
		case A:
			return TEAM.A;
		case B:
			return TEAM.B;
		default:
			return null;
		}
	}

	private class RobotStat {
		public final RobotType type;
		public final Team team;
		private double flux;
		private boolean on;
		private MapLocation location;

		public RobotStat(Team team, RobotType type) {
			this.team = team;
			this.type = type;
		}

		public void setFlux(double flux) {
			this.flux = flux;
		}

		public void setLocation(MapLocation location) {
			this.location = location;
		}

		public MapLocation getLocation() {
			return location;
		}

		public boolean isOn() {
			return on;
		}

		/**
		 * 
		 * @return true if the status has changed
		 */
		public boolean recalculateOnStatus() {
			boolean newOn = type == RobotType.ARCHON || type == RobotType.TOWER || flux > GameConstants.UNIT_UPKEEP;
			if (newOn != on) {
				on = newOn;
				return true;
			}
			return false;
		}
	}

}
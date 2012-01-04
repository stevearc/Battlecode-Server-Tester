package worker;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import model.MatchResult;
import model.TEAM;
import model.TeamMatchResult;
import model.MatchResult.WIN_CONDITION;
import battlecode.common.Chassis;
import battlecode.common.GameConstants;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.serial.GameStats;
import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.XStreamProxy;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.SpawnSignal;
import battlecode.world.signal.TurnOffSignal;
import battlecode.world.signal.TurnOnSignal;

/**
 * This class reads and analyzes rms (match) files.  It links against battlecode-server.jar
 * @author stevearc
 *
 */
public class GameData extends Proxy {
	// Maps robotID to info
	private HashMap<Integer, RobotStat> robots = new HashMap<Integer, RobotStat>();

	private MatchFooter footer;
	private ArrayList<RoundDelta> rounds = new ArrayList<RoundDelta>();
	private ArrayList<RoundStats> stats = new ArrayList<RoundStats>();
	private GameStats gameStats;
	
	public GameData(){
		
	}

	public GameData(String filename) throws IOException, ClassNotFoundException {
		ObjectInputStream input = null;
		input = XStreamProxy.getXStream().createObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));

		Object o;
		try {
			while ((o = input.readObject()) != null) {
				if (o instanceof RoundDelta) {
					rounds.add((RoundDelta) o);
				} else if (o instanceof RoundStats) {
					stats.add((RoundStats) o);
				} else if (o instanceof MatchFooter) {
					footer = (MatchFooter) o;
				} else if (o instanceof GameStats) {
					gameStats = (GameStats) o;
				}
			}
		} catch (EOFException e) {
			// Aaaaaand we're done
		}
	}

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

	@Override
	public void writeObject(Object o) {
		if (o instanceof GameStats) {
			gameStats = (GameStats) o;
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
		this.stats.add(stats);
	}

	@SuppressWarnings("unchecked")
	public MatchResult analyzeMatch() {
		MatchResult matchResult = new MatchResult();
		RobotStat r;
		Team[] teams = Team.values();
		TeamMatchResult[] teamResults = new TeamMatchResult[teams.length];
		ArrayList<Integer>[] activeRobots = new ArrayList[teams.length];
		Integer[] currentActiveRobots = new Integer[teams.length];
		ArrayList<Double>[] fluxDrain = new ArrayList[teams.length];
		Double[] currentFluxDrain = new Double[teams.length];
		ArrayList<Double>[] fluxIncome = new ArrayList[teams.length];
		Double[] currentFluxIncome = new Double[teams.length];
		ArrayList<Double>[] fluxReserve = new ArrayList[teams.length];
		Double[] currentFluxReserve = new Double[teams.length];

		for (int i = 0; i < teams.length; i++) {
			teamResults[i] = new TeamMatchResult();
			activeRobots[i] = new ArrayList<Integer>();
			currentActiveRobots[i] = 0;
			fluxDrain[i] = new ArrayList<Double>();
			currentFluxDrain[i] = 0.0;
			fluxIncome[i] = new ArrayList<Double>();
			currentFluxIncome[i] = 0.0;
			fluxReserve[i] = new ArrayList<Double>();
			currentFluxReserve[i] = GameConstants.INITIAL_FLUX;
		}

		RoundDelta round;
		RoundStats stat;
		for (int l = 0; l < rounds.size(); l++) {
			round = rounds.get(l);
			stat = stats.get(l);
			for (int i = 0; i < 2; i++) {
				currentFluxIncome[i] = stat.getGatheredPoints(teams[i])/100.;
				currentFluxReserve[i] = stat.getPoints(teams[i])/100.;
			}

			Signal[] signals = round.getSignals();
			for (int i = 0; i < signals.length; i++) {
				Signal signal = signals[i];
				if (signal instanceof SpawnSignal) {
					SpawnSignal s = (SpawnSignal)signal;
					currentActiveRobots[s.getTeam().ordinal()]++;
					currentFluxDrain[s.getTeam().ordinal()] += s.getType().upkeep;

					r = new RobotStat(s.getTeam(), s.getType());
					robots.put(s.getRobotID(), r);
				} 
				else if(signal instanceof DeathSignal) {
					DeathSignal s = (DeathSignal)signal;
					r = robots.remove(s.getObjectID());
					if (r.on) {
						currentActiveRobots[r.team.ordinal()]--;
						currentFluxDrain[r.team.ordinal()] -= r.type.upkeep;
					}
				} 
				else if (signal instanceof TurnOffSignal) {
					TurnOffSignal s = (TurnOffSignal)signal;
					r = robots.get(s.robotID);
					r.on = false;
					currentActiveRobots[r.team.ordinal()]--;
					currentFluxDrain[r.team.ordinal()] -= r.type.upkeep;
				} 
				else if (signal instanceof TurnOnSignal) {
					TurnOnSignal s = (TurnOnSignal)signal;
					for (Integer id: s.robotIDs) {
						r = robots.get(id);
						if (!r.on) {
							r.on = true;
							currentActiveRobots[r.team.ordinal()]++;
							currentFluxDrain[r.team.ordinal()] += r.type.upkeep;
						}
					}
				}
			}

			for (int i = 0; i < teams.length; i++) {
				activeRobots[i].add(currentActiveRobots[i]);
				fluxDrain[i].add(currentFluxDrain[i]);
				fluxIncome[i].add(currentFluxIncome[i]);
				fluxReserve[i].add(currentFluxReserve[i] + currentFluxIncome[i] - currentFluxDrain[i]);
			}
		}
		for (int i = 0; i < teams.length; i++) {
			teamResults[i].setActiveRobots(activeRobots[i].toArray(new Integer[activeRobots[i].size()]));
			teamResults[i].setFluxDrain(fluxDrain[i].toArray(new Double[fluxDrain[i].size()]));
			teamResults[i].setFluxIncome(fluxIncome[i].toArray(new Double[fluxIncome[i].size()]));
			teamResults[i].setFluxReserve(fluxReserve[i].toArray(new Double[fluxReserve[i].size()]));
		}

		matchResult.setRounds(new Long(rounds.size()));
		matchResult.setWinner(convertTeam(footer.getWinner()));
		switch (gameStats.getDominationFactor()) {
		case DESTROYED:
			matchResult.setWinCondition(WIN_CONDITION.DESTROY);
			break;
		case OWNED:
			matchResult.setWinCondition(WIN_CONDITION.POINTS);
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
		public final Chassis type;
		public final Team team;
		public boolean on;

		public RobotStat(Team team, Chassis type) {
			this.team = team;
			this.type = type;
			on = true;
		}
	}

}
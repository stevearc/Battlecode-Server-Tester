package worker;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import model.MatchResult;
import model.TEAM;
import model.TeamMatchResult;
import model.MatchResult.WIN_CONDITION;
import battlecode.common.Chassis;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.serial.MatchFooter;
import battlecode.serial.RoundDelta;
import battlecode.server.proxy.XStreamProxy;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.EquipSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.SpawnSignal;
import battlecode.world.signal.TurnOffSignal;
import battlecode.world.signal.TurnOnSignal;

/**
 * This class reads and analyzes rms (match) files.  It links against battlecode-server.jar
 * @author stevearc
 *
 */
public class GameData {
	// Maps robotID to location
	private HashMap<Integer, RobotStat> robots = new HashMap<Integer, RobotStat>();
	private ArrayList<RoundDelta> rounds = new ArrayList<RoundDelta>();
	private MatchFooter footer;

	public GameData(String filename) throws IOException, ClassNotFoundException {
		ObjectInputStream input = null;
		input = XStreamProxy.getXStream().createObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));

		Object o;
		try {
			while ((o = input.readObject()) != null) {
				if (o instanceof RoundDelta) {
					rounds.add((RoundDelta)o);
				} else if (o instanceof MatchFooter) {
					footer = (MatchFooter)o;
				}
			}
		} catch (EOFException e) {
			// Aaaaaand we're done
		}
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

		for (int i = 0; i < teams.length; i++) {
			teamResults[i] = new TeamMatchResult();
			activeRobots[i] = new ArrayList<Integer>();
			currentActiveRobots[i] = 0;
			fluxDrain[i] = new ArrayList<Double>();
			currentFluxDrain[i] = 0.0;
		}

		for (RoundDelta round: rounds) {
			Signal[] signals = round.getSignals();

			for (int i = 0; i < signals.length; i++) {
				Signal signal = signals[i];
				if (signal instanceof SpawnSignal) {
					SpawnSignal s = (SpawnSignal)signal;
					currentActiveRobots[s.getTeam().ordinal()]++;
					currentFluxDrain[s.getTeam().ordinal()] += s.getType().upkeep;
					r = new RobotStat(s.getTeam(), s.getType());
					robots.put(s.getRobotID(), r);
				} else if(signal instanceof EquipSignal) {
					// pass for now
				} else if(signal instanceof MovementSignal) {
					// pass for now
				} else if(signal instanceof AttackSignal) {
					// pass for now
				} else if(signal instanceof DeathSignal) {
					DeathSignal s = (DeathSignal)signal;
					r = robots.remove(s.getObjectID());
					currentActiveRobots[r.team.ordinal()]--;
					currentFluxDrain[r.team.ordinal()] -= r.type.upkeep;
				} else if (signal instanceof TurnOffSignal) {
					TurnOffSignal s = (TurnOffSignal)signal;
					r = robots.get(s.robotID);
					currentActiveRobots[r.team.ordinal()]--;
					currentFluxDrain[r.team.ordinal()] -= r.type.upkeep;
				} else if (signal instanceof TurnOnSignal) {
					TurnOnSignal s = (TurnOnSignal)signal;
					for (Integer id: s.robotIDs) {
						r = robots.get(id);
						currentActiveRobots[r.team.ordinal()]++;
						currentFluxDrain[r.team.ordinal()] += r.type.upkeep;
					}
				}
			}

			for (int i = 0; i < teams.length; i++) {
				activeRobots[i].add(currentActiveRobots[i]);
				fluxDrain[i].add(currentFluxDrain[i]);
			}
		}
		for (int i = 0; i < teams.length; i++) {
			teamResults[i].setActiveRobots(activeRobots[i]);
			teamResults[i].setFluxDrain(fluxDrain[i]);
		}

		matchResult.setRounds(new Long(rounds.size()));
		matchResult.setWinner(convertTeam(footer.getWinner()));
		if (currentActiveRobots[Team.A.ordinal()] == 0 || currentActiveRobots[Team.B.ordinal()] == 0) {
			matchResult.setWinCondition(WIN_CONDITION.DESTROY);
		} else {
			matchResult.setWinCondition(WIN_CONDITION.POINTS);
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
		public Chassis type;
		public final Team team;

		public RobotStat(Team team, Chassis type) {
			this.team = team;
			transform(type);
		}

		public void transform(Chassis type) {
			this.type=type;
		}

	}
}
package backend;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import networking.CometCmd;
import networking.CometMessage;
import networking.Packet;

import common.BattlecodeMap;
import common.Config;
import common.Match;
import common.MatchSet;

import db.Database;

/**
 * Handles distribution of load to connected Clients
 * @author steven
 *
 */
public class Server {
	private Config config;
	private Database db;
	private Logger _log;
	private NetworkHandler handler;
	private HashSet<ClientRepr> clients = new HashSet<ClientRepr>();
	private HashSet<BattlecodeMap> maps = new HashSet<BattlecodeMap>();
	private WebPollHandler wph;

	public Server() throws Exception {
		config = Config.getConfig();
		db = Config.getDB();
		wph = Config.getWebPollHandler();
		_log = config.getLogger();
		handler = new NetworkHandler();
		new Thread(handler).start();
		new Thread(new Updater(this)).start();
	}

	public synchronized void poke() {
		_log.info("poke");
		try {
			startRun();
		} catch (SQLException e) {
		}
	}

	public synchronized void queueRun(MatchSet run) {
		try {
			PreparedStatement st = db.prepare("INSERT INTO runs (team_a, team_b) VALUES (?, ?)");
			st.setString(1, run.getTeam_a());
			st.setString(2, run.getTeam_b());
			db.update(st, true);
			ResultSet rs = db.query("SELECT MAX(id) as newest FROM runs"); 
			rs.next();
			int id = rs.getInt("newest");
			wph.broadcastMsg("matches", new CometMessage(CometCmd.INSERT_TABLE_ROW, new Object[] {id, 
					run.getTeam_a(), run.getTeam_b()}));
			startRun();
		} catch (SQLException e) {
		}
	}

	public synchronized void deleteRun(int runId) {
		try {
			if (runId == getCurrentId()) {
				stopCurrentRun();
				startRun();
			} else {
				db.update("DELETE FROM matches WHERE run_id = " + runId, true);
				db.update("DELETE FROM runs WHERE id = " + runId, true);
				wph.broadcastMsg("matches", new CometMessage(CometCmd.DELETE_TABLE_ROW, new Object[] {runId}));
			}
		} catch (SQLException e) {
		}
	}

	public synchronized void clientConnect(ClientRepr client) {
		_log.info("Client connected: " + client);
		clients.add(client);
		sendClientMatches(client);
	}

	public synchronized void clientDisconnect(ClientRepr client) {
		_log.info("Client disconnected: " + client);
		clients.remove(client);
	}

	public synchronized void matchFinished(ClientRepr client, Packet p) {
		Match m = (Match) p.get(0);
		String status = (String) p.get(1);
		int winner = (Integer) p.get(2);
		int win_condition = (Integer) p.get(3);
		int a_points = (Integer) p.get(4);
		int b_points = (Integer) p.get(5);
		byte[] data = (byte[]) p.get(6);
		try {
			if ("ok".equals(status) && m.run_id == getCurrentId()) {
				if (!getMatchesLeft().contains(m)) {
					_log.info("Received duplicate match: " + m);
					sendClientMatches(client);
					return;
				}
				_log.info("Match finished: " + m + " winner: " + (winner == 1 ? "A" : "B"));
				PreparedStatement stmt = null;
				stmt = db.prepare("INSERT INTO matches (run_id, map, reverse, win, win_condition, height, width, rounds, points, a_points, b_points, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				stmt.setInt(1, m.run_id);
				stmt.setString(2, m.map.map);
				stmt.setInt(3, m.reverse);
				stmt.setInt(4, winner);
				stmt.setInt(5, win_condition);
				stmt.setInt(6, m.map.height);
				stmt.setInt(7, m.map.width);
				stmt.setInt(8, m.map.rounds);
				stmt.setInt(9, m.map.points);
				stmt.setInt(10, a_points);
				stmt.setInt(11, b_points);
				stmt.setBinaryStream(12, new ByteArrayInputStream(data), data.length);
				db.update(stmt, true);
				wph.broadcastMsg("matches", new CometMessage(CometCmd.MATCH_FINISHED, new Object[] {m.run_id, winner}));

				// If finished, start next run
				if (getMatchesLeft().isEmpty()) {
					stopCurrentRun();
					startRun();
				}

			} else {
				_log.warning("Match " + m + " on client " + client + " failed");
			}

			if (getMatchesLeft().isEmpty()) {
				stopCurrentRun();
				startRun();
			} else {
				sendClientMatches(client);
			}
		} catch (SQLException e) {
		}
	}

	public synchronized String debugDump() {
		StringBuilder sb = new StringBuilder();
		sb.append("Maps: " + maps);
		sb.append("\n");
		try {
			if (getCurrentId() != -1) {
				sb.append("Matches left: " + getMatchesLeft());
				sb.append("\n");
				sb.append("Matches left not running: " + getMatchesLeftAndNotRunning());
				sb.append("\n");
			}
		} catch (SQLException e) {
		}
		sb.append("Clients: " + clients);
		sb.append("\n");
		return sb.toString();
	}

	public synchronized void sendClientMatches(ClientRepr client) {
		try {
			HashSet<Match> matches = getMatchesLeftAndNotRunning();
			for (Match m: matches) {
				if (!client.isFree())
					break;
				_log.info("Sending match " + m + " to client " + client);
				client.runMatch(m);
			}

			// If we are currently running all necessary maps, add some redundancy
			Match[] matchIndex = getMatchesLeft().toArray(new Match[0]);
			Random r = new Random();
			if (client.isFree()) {
				client.runMatch(matchIndex[r.nextInt(matchIndex.length)]);
			}
		} catch (SQLException e) {
		}
	}

	private void startRun() throws SQLException {
		updateMaps();
		if (getCurrentId() != -1)
			return;

		ResultSet rs = db.query("SELECT * FROM runs WHERE status = 0 ORDER BY id");
		if (!rs.next()) {
			return;
		}
		MatchSet run = new MatchSet(rs.getString("team_a"), rs.getString("team_b"));
		db.update("UPDATE runs SET status = 1, started = NOW() WHERE id = " + rs.getInt("id"), true);
		if (!validateTeams(run)) {
			PreparedStatement stmt = db.prepare("UPDATE runs SET status = 3 WHERE team_a LIKE ? AND team_b LIKE ?");
			stmt.setString(1, run.getTeam_a());
			stmt.setString(2, run.getTeam_b());
			db.update(stmt, true);
			wph.broadcastMsg("matches", new CometMessage(CometCmd.RUN_ERROR, new Object[] {rs.getInt("id")}));
			startRun();
		} else {
			wph.broadcastMsg("matches", new CometMessage(CometCmd.START_RUN, new Object[] {rs.getInt("id")}));
			for (ClientRepr c: clients) {
				sendClientMatches(c);
			}
		}
	}

	private int getCurrentId() throws SQLException {
		ResultSet rs = db.query("SELECT id FROM runs WHERE status = 1");
		if (rs.next()) {
			return rs.getInt("id");
		} else {
			return -1;
		}
	}

	private void stopCurrentRun() throws SQLException {
		_log.info("Stopping current run");
		int id = getCurrentId();
		db.update("UPDATE runs SET status = 2, ended = NOW() WHERE id = " + id, true);
		wph.broadcastMsg("matches", new CometMessage(CometCmd.FINISH_RUN, new Object[] {id}));
		for (ClientRepr c: clients) {
			c.stopAllMatches();
		}
	}

	private HashSet<Match> getMatchesLeft() throws SQLException {
		ResultSet rs = db.query("SELECT id, team_a, team_b FROM runs WHERE status = 1");
		rs.next();
		int run = rs.getInt("id");
		String team_a = rs.getString("team_a");
		String team_b = rs.getString("team_b");
		rs.close();
		PreparedStatement st = db.prepare("SELECT map, reverse FROM matches WHERE run_id = ?");
		st.setInt(1, run);
		rs = db.query(st);
		HashSet<Match> finishedMatches = new HashSet<Match>();
		while (rs.next()) {
			finishedMatches.add(new Match(run, team_a, team_b, new BattlecodeMap(rs.getString("map"), 0, 0, 0, 0), rs.getInt("reverse")));
		}
		HashSet<Match> matchesLeft = new HashSet<Match>();
		for (BattlecodeMap map: maps) {
			Match m1 = new Match(run, team_a, team_b, map, 0);
			if (!finishedMatches.contains(m1)){
				matchesLeft.add(m1);
			}
			Match m2 = new Match(run, team_a, team_b, map, 1);
			if (!finishedMatches.contains(m2)){
				matchesLeft.add(m2);
			}
		}
		return matchesLeft;
	}

	private HashSet<Match> getMatchesLeftAndNotRunning() throws SQLException {
		HashSet<Match> matchesLeft = getMatchesLeft();
		for (ClientRepr c: clients) {
			for (Match m: c.getRunningMatches()) {
				matchesLeft.remove(m);
			}
		}
		return matchesLeft;
	}

	private boolean validateTeams(MatchSet run) throws SQLException {
		updateRepo();

		String team_a = run.getTeam_a();
		String team_b = run.getTeam_b();
		PreparedStatement st = db.prepare("SELECT * FROM tags WHERE tag LIKE ? OR alias LIKE ?");
		st.setString(1, team_a);
		st.setString(2, team_a);
		ResultSet rs = db.query(st);

		if (!rs.next()) {
			st.close();
			return false;
		}
		st.close();
		PreparedStatement stmt = db.prepare("SELECT * FROM tags WHERE tag LIKE ? OR alias LIKE ?");
		stmt.setString(1, team_b);
		stmt.setString(2, team_b);
		rs = db.query(stmt);
		if (!rs.next()) {
			stmt.close();
			return false;
		}
		stmt.close();
		return true;
	}

	public synchronized void updateRepo() throws SQLException {
		try {
			Process p = Runtime.getRuntime().exec(new String[] {config.cmd_update, "server"});

			p.waitFor();
			BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()));
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				PreparedStatement st = db.prepare("INSERT INTO tags (tag) VALUES (?)");
				st.setString(1, line);
				db.update(st, false);
			}
		} catch (InterruptedException e){
			_log.log(Level.WARNING, "Error updating repo", e);
		} catch (IOException e) {
			_log.log(Level.WARNING, "Error updating repo", e);
		}
	}

	public synchronized void updateMaps() {
		File file = new File(config.repo + "/maps");
		File[] mapFiles = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		maps.clear();
		for (File m: mapFiles) {
			try {
				maps.add(new BattlecodeMap(m));
			} catch (Exception e) {
				_log.log(Level.WARNING, "Error parsing map", e);
			}
		}
	}

	public Set<ClientRepr> getConnections() {
		return clients;
	}

	public double getProgress() throws SQLException {
		double left = getMatchesLeft().size();
		double total = maps.size();
		double progress = total - left;
		return progress/total;
	}
	
	public HashSet<BattlecodeMap> getMaps() {
		return maps;
	}

}

/**
 * Periodically updates the repo
 * @author steven
 * The reason this is necessary is if the user deletes a map from the repo.  The clients will pull
 * the updated repo without the map, but the server will still attempt to make them run a match on that map.
 * If the server periodically updates (instead of just at the beginning of a run), this error will not require
 * a restart.
 */
class Updater implements Runnable {
	private Server server;

	public Updater(Server server) {
		this.server = server;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(300000);
				server.updateRepo();
				server.updateMaps();
			} catch (InterruptedException e) {
			} catch (SQLException e) {
			}
		}
	}


}
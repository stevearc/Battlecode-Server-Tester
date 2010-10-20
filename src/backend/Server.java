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

import networking.Packet;

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
	private HashSet<String> maps = new HashSet<String>();

	public Server() throws Exception {
		config = Config.getConfig();
		db = Config.getDB();
		_log = config.getLogger();
		handler = new NetworkHandler();
		new Thread(handler).start();
	}

	public synchronized void poke() {
		_log.info("Server.poke()");
		try {
			startRun();
		} catch (SQLException e) {
		}
	}

	public synchronized void queueRun(MatchSet run) {
		_log.info("Server.queueRun()");
		try {
			PreparedStatement st = db.prepare("INSERT INTO runs (team_a, team_b) VALUES (?, ?)");
			st.setString(1, run.getTeam_a());
			st.setString(2, run.getTeam_b());
			db.update(st, true);

			startRun();
		} catch (SQLException e) {
		}
	}

	public synchronized void deleteRun(int runId) {
		_log.info("Server.deleteRun()");
		try {
			if (runId == getCurrentId()) {
				stopCurrentRun();
				startRun();
			} else {
				db.update("DELETE FROM matches WHERE run_id = " + runId, true);
				db.update("DELETE FROM runs WHERE id = " + runId, true);
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
		byte[] data = (byte[]) p.get(3);
		try {
			if ("ok".equals(status) && m.run_id == getCurrentId()) {
				if (!getMapsLeft(m.run_id).contains(m.map)) {
					_log.info("Received duplicate match: " + m);
					sendClientMatches(client);
					return;
				}
				_log.info("Match finished: " + m + " winner: " + (winner == 1 ? "A" : "B"));
				PreparedStatement stmt = null;
				stmt = db.prepare("INSERT INTO matches (run_id, map, win, data) VALUES (?, ?, ?, ?)");
				stmt.setInt(1, m.run_id);
				stmt.setString(2, m.map);
				stmt.setInt(3, winner);
				stmt.setBinaryStream(4, new ByteArrayInputStream(data), data.length);
				db.update(stmt, true);

				// If finished, start next run
				if (getMapsLeft(m.run_id).isEmpty()) {
					stopCurrentRun();
					startRun();
				}

			} else {
				_log.warning("Match " + m + " on client " + client + " failed");
			}

			if (getMapsLeft(getCurrentId()).isEmpty()) {
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
				sb.append("Maps left: " + getMapsLeft(getCurrentId()));
				sb.append("\n");
				sb.append("Maps left not running: " + getMapsLeftAndNotRunning(getCurrentId()));
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
			ResultSet rs = db.query("SELECT * FROM runs WHERE status = 1");
			if (!rs.next())
				return;
			int run_id = rs.getInt("id");
			String team_a;

			team_a = rs.getString("team_a");

			String team_b = rs.getString("team_b");
			HashSet<String> maps = getMapsLeftAndNotRunning(run_id);
			for (String m: maps) {
				if (!client.isFree())
					break;
				_log.info("Sending map " + m + " to client " + client);
				client.runMatch(new Match(run_id, team_a, team_b, m));
			}

			// If we are currently running all necessary maps, add some redundancy
			String[] mapIndex = getMapsLeft(run_id).toArray(new String[0]);
			Random r = new Random();
			if (client.isFree()) {
				client.runMatch(new Match(run_id, team_a, team_b, mapIndex[r.nextInt(mapIndex.length)]));
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
			return;
		}
		for (ClientRepr c: clients) {
			sendClientMatches(c);
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
		db.update("UPDATE runs SET status = 2, ended = NOW() WHERE id = " + getCurrentId(), true);
		for (ClientRepr c: clients) {
			c.stopAllMatches();
		}
	}

	private HashSet<String> getMapsLeft(int run) throws SQLException {
		PreparedStatement st = db.prepare("SELECT map FROM matches WHERE run_id = ?");
		st.setInt(1, run);
		ResultSet rs = db.query(st);
		HashSet<String> finishedMaps = new HashSet<String>();
		while (rs.next()) {
			finishedMaps.add(rs.getString("map"));
		}
		HashSet<String> mapsLeft = new HashSet<String>();
		for (String s: maps) {
			if (!finishedMaps.contains(s)){
				mapsLeft.add(s);
			}
		}
		return mapsLeft;
	}

	private HashSet<String> getMapsLeftAndNotRunning(int run) throws SQLException {
		HashSet<String> mapsLeft = getMapsLeft(run);
		for (ClientRepr c: clients) {
			for (Match m: c.getRunningMatches()) {
				mapsLeft.remove(m.map);
			}
		}
		return mapsLeft;
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

	private void updateRepo() throws SQLException {
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

	private void updateMaps() {
		File file = new File(config.repo + "/maps");
		String[] mapFiles = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		maps.clear();
		for (String m: mapFiles) {
			maps.add(m.substring(0, m.length() - 4));
		}
	}

	public Set<ClientRepr> getConnections() {
		return clients;
	}

}
package backend;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import networking.Controller;
import networking.Network;
import networking.Packet;
import networking.PacketType;

import common.Config;

import db.Database;

/**
 * Handles distribution of load to connected Clients
 * @author steven
 *
 */
public class Server implements Controller, Runnable{
	private Logger _log;
	private NetworkHandler handler;
	// Mapping of all clients to the maps they are currently running
	private HashMap<Network, HashSet<String>> clients = new HashMap<Network, HashSet<String>>();
	// List of all maps that have yet to be run
	private LinkedList<String> queued = new LinkedList<String>();
	// Queue of messages from clients
	private Vector<Message> messageQueue = new Vector<Message>();
	// Set of all clients that have requested a map
	private HashSet<Network> availableClients = new HashSet<Network>();
	private Config config;
	private Database db;
	private String[] maps;
	private boolean runCurrentMatch = true;

	public Server() throws Exception {
		config = Config.getConfig();
		db = Config.getDB();
		_log = config.getLogger();
		handler = new NetworkHandler(this);
		new Thread(handler).start();
	}

	/**
	 * Choose a map and send this client a message to run a match on it
	 * @param net
	 * @param team_a
	 * @param team_b
	 * @throws SQLException 
	 */
	private void sendMatchToClient(Network net, String team_a, String team_b) throws SQLException {
		String map = queued.poll();
		if (map != null) {
			_log.info("Sending map: " + map + " to " + net);
			net.send(new Packet(PacketType.SEND_MAP, map, team_a, team_b, false, null));
			clients.get(net).add(map);
			availableClients.remove(net);
			PreparedStatement st = db.prepare("INSERT INTO running_matches (conn_id, map) VALUES (?, ?)");
			st.setInt(1, idFromAddr(net.toSQL()));
			st.setString(2, map);
			db.update(st, true);
		}
	}

	/**
	 * Get the connection id of an address
	 * @param addr
	 * @return
	 * @throws SQLException 
	 */
	private int idFromAddr(String addr) throws SQLException {
		int id = -1;
		PreparedStatement st = db.prepare("SELECT id FROM connections WHERE addr LIKE ?");
		st.setString(1, addr);
		ResultSet rs = db.query(st);
		if (rs.next()) {
			id = rs.getInt("id");
		}
		rs.close();
		st.close();
		return id;
	}

	/**
	 * Save the results of a match to a file and the database
	 * @param m
	 * @param runid
	 * @throws SQLException 
	 */
	private void saveMatchResults(final Message m, int runid) throws SQLException {
		clients.get(m.receivedBy).remove(m.map);
		PreparedStatement st = db.prepare("DELETE FROM running_matches WHERE map LIKE ?");
		st.setString(1, m.map);
		db.update(st, true);

		if (m.match == null) {
			_log.info(m.map + " did not finish correctly");
			queued.add(m.map);
			return;
		} else {
			PreparedStatement stmt = null;
			stmt = db.prepare("INSERT INTO matches (run_id, map, win, data) VALUES (?, ?, ?, ?)");
			stmt.setInt(1, runid);
			stmt.setString(2, m.map);
			stmt.setInt(3, (m.a_wins ? 1 : 0));
			stmt.setBinaryStream(4, new ByteArrayInputStream(m.match), m.match.length);
			db.update(stmt, true);

		}
	}

	/**
	 * Check for newly connected clients and add them to our pool
	 * @throws SQLException 
	 */
	private void addClients() throws SQLException {
		Network net = handler.getNewClient();
		if (net != null) {
			_log.info("Adding a new client: " + net);
			clients.put(net, new HashSet<String>());
			PreparedStatement st = db.prepare("INSERT INTO connections (addr) VALUES (?)");
			st.setString(1, net.toSQL());
			db.update(st, true);
		}
	}

	/**
	 * Find an idling client
	 * @return The first Network whose client is idle, or null if none are idle
	 */
	private Network getIdleClient() {
		for (Network net: availableClients)
			return net;
		return null;
	}

	/**
	 * Find any connections that are no longer active and remove them from our pool
	 * @throws SQLException 
	 */
	private void cleanDeadClients() throws SQLException {
		ArrayList<Network> deadClients = new ArrayList<Network>();
		for (Network network: clients.keySet()) {
			if (!network.isConnected()) {
				deadClients.add(network);
				continue;
			}
		}
		for (Network net: deadClients) {
			PreparedStatement st = db.prepare("DELETE FROM running_matches WHERE conn_id = ?");
			st.setInt(1, idFromAddr(net.toSQL()));
			db.update(st, true);
			PreparedStatement st2 = db.prepare("DELETE FROM connections WHERE addr LIKE ?");
			st2.setString(1, net.toSQL());
			db.update(st2, true);
			for (String map: clients.get(net))
				queued.add(map);
			clients.remove(net);
			net.stop();
		}
		deadClients.clear();
	}

	/**
	 * Check to see if any clients are taking too long to run a match
	 * @throws SQLException 
	 */
	private void checkMatchTimeout() throws SQLException {
		ArrayList<String> timedOut = new ArrayList<String>();
		ResultSet rs = db.query("SELECT map, now() as now, modified FROM connections c " +
		"JOIN running_matches r ON c.id = r.conn_id");
		while (rs.next()) {
			Timestamp mod = rs.getTimestamp("modified");
			Timestamp now = rs.getTimestamp("now");
			long seconds = (now.getTime() - mod.getTime())/1000;
			if (seconds > config.timeout + 10) { // add 10 to allow for network latency
				timedOut.add(rs.getString("map"));
			}
		}
		rs.close();
		for (String map: timedOut) {
			_log.warning("Client timed out on map " + map);
			for (HashSet<String> maps: clients.values()) {
				if (maps.remove(map))
					break;
			}
			queued.add(map);
			PreparedStatement st = db.prepare("DELETE FROM running_matches WHERE map LIKE ?");
			st.setString(1, map);
			db.update(st, true);
		}
	}

	@Override
	public void addPacket(Network network, Packet p) {
		Message m = new Message(p, network);
		m.receivedBy = network;
		messageQueue.add(m);
	}

	/**
	 * Delete all Database data associated with a run
	 * @param runid
	 * @throws SQLException 
	 */
	private void deleteRun(int runid) throws SQLException {
		PreparedStatement st1 = db.prepare("DELETE FROM matches WHERE run_id = ?");
		st1.setInt(1, runid);
		db.update(st1, true);
		PreparedStatement st2 = db.prepare("DELETE FROM runs WHERE id = ?");
		st2.setInt(1, runid);
		db.update(st2, true);
	}

	private void clearUnfinishedRuns() throws SQLException {
		ResultSet rs = db.query("SELECT id FROM runs WHERE finished = 0");
		while (rs.next()) {
			int runid = rs.getInt("id");
			_log.info("Clearing out unfinished run: " + runid);
			deleteRun(runid);
		}
		rs.close();
	}

	private void handlePackets(int runid, String team_a, String team_b) throws SQLException {
		while (messageQueue.size() > 0) {
			Message m = messageQueue.firstElement();
			// Client wants another map
			if (m.type == PacketType.REQUEST_MAP) {
				availableClients.add(m.receivedBy);
			}
			// Only save the match results if all data matches
			else if (m.type == PacketType.MAP_RESULT && clients.get(m.receivedBy).contains(m.map)
					&& team_a.equals(m.team_a) && team_b.equals(m.team_b)) {
				saveMatchResults(m, runid);
			}
			messageQueue.remove(0);
		}
	}

	private void resetRun() throws SQLException {
		queued.clear();
		availableClients.clear();
		// Tell all clients to stop any current runs they may have
		for (Network net: clients.keySet()) {
			net.send(new Packet(PacketType.RESET_RUNS, null, null, null, true, null));
		}
		db.update("DELETE FROM connections", true);
		db.update("DELETE FROM running_matches", true);
		for (Network net: clients.keySet()) {
			PreparedStatement st = db.prepare("INSERT INTO connections (addr) VALUES (?)");
			st.setString(1, net.toSQL());
			db.update(st, true);
		}
	}

	/**
	 * 
	 * @return The number of matches the connected clients are currently running
	 */
	private int getNumMatchesCurrentlyRunning() {
		int cumulative = 0;
		for (HashSet<String> maps: clients.values()) {
			cumulative += maps.size();
		}
		return cumulative;
	}
	
	public synchronized void cancelCurrent() {
		runCurrentMatch = false;
	}
	
	private synchronized void manageRunMatch(int runid, String team_a, String team_b) throws SQLException {
		if (!runCurrentMatch)
			return;
		addClients();
		handlePackets(runid, team_a, team_b);
		cleanDeadClients();
		checkMatchTimeout();
		Network idleClient = getIdleClient();
		if (idleClient != null) {
			sendMatchToClient(idleClient, team_a, team_b);
		}
	}

	private void runMatch(String team_a, String team_b) throws SQLException {
		_log.info("Running " + team_a + " vs " + team_b);
		resetRun();
		File file = new File(config.repo + "/maps");
		maps = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.toLowerCase().endsWith(".xml"));
			}
		});
		for (int i = 0; i < maps.length; i++) {
			maps[i] = maps[i].substring(0, maps[i].length() - 4);
		}
		for (String map: maps)
			queued.add(map);
		// Clear out old messages because we don't care anymore
		messageQueue.clear();
		int runid;
		PreparedStatement st = db.prepare("INSERT INTO runs (team_a, team_b, started) VALUES " + 
				"(?, ?, now())");
		st.setString(1, team_a);
		st.setString(2, team_b);
		db.update(st, true);
		ResultSet rs = db.query("SELECT MAX(id) as m FROM runs");
		if (rs.next())
			runid = rs.getInt("m");
		else {
			_log.severe("Could not find max value in table 'runs'");
			return;
		}
		rs.close();

		while (runCurrentMatch && (!queued.isEmpty() || getNumMatchesCurrentlyRunning() != 0)) {
			manageRunMatch(runid, team_a, team_b);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				_log.warning("Interrupt exception");
			}
		}
		PreparedStatement stmt = db.prepare("UPDATE runs SET finished = 1, ended = now() WHERE id = ?");
		stmt.setInt(1, runid);
		db.update(stmt, true);
		_log.info("Finished running " + team_a + " vs " + team_b);
		resetRun();
	}

	private String[] validateTeams(String team_a, String team_b) throws SQLException {
		String[] teams = new String[2];
		HashSet<String> tags = new HashSet<String>();
		HashMap<String, String> aliases = new HashMap<String, String>();
		ResultSet rs = db.query("SELECT tag, alias FROM tags");
		while (rs.next()) {
			String tag = rs.getString("tag");
			String alias = rs.getString("alias");
			tags.add(tag);
			aliases.put(alias, tag);
		} 
		rs.close();
		if (tags.contains(team_a)) {
			teams[0] = team_a;
		} else if (aliases.containsKey(team_a)) {
			teams[0] = aliases.get(team_a);
		} else {
			_log.warning("Unrecognized tag: " + team_a);
			return null;
		}

		if (tags.contains(team_b)) {
			teams[1] = team_b;
		} else if (aliases.containsKey(team_b)) {
			teams[1] = aliases.get(team_b);
		} else {
			_log.warning("Unrecognized tag: " + team_b);
			return null;
		}

		return teams;
	}

	public void run() {
		try {
			db.update("DELETE FROM connections", true);
			db.update("DELETE FROM running_matches", true);
			while (true) {
				clearUnfinishedRuns();
				addClients();
				cleanDeadClients();
				ResultSet rs = db.query("SELECT id, team_a, team_b FROM queue ORDER BY id LIMIT 1");
				int id = 0;
				if (rs.next()) {
					Process p = Runtime.getRuntime().exec(new String[] {config.cmd_update, "server"});
					p.waitFor();
					BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()));
					for (String line = read.readLine(); line != null; line = read.readLine()) {
						PreparedStatement st = db.prepare("INSERT INTO tags (tag) VALUES (?)");
						st.setString(1, line);
						db.update(st, false);
					}
					String team_a = rs.getString("team_a");
					String team_b = rs.getString("team_b");
					id = rs.getInt("id");
					PreparedStatement st = db.prepare("DELETE FROM queue WHERE id = ?");
					st.setInt(1, id);
					db.update(st, true);
					String[] teamNames = validateTeams(team_a, team_b);
					if (teamNames != null) {
						runCurrentMatch = true;
						runMatch(teamNames[0], teamNames[1]);
					} else {
						PreparedStatement stmt = db.prepare("INSERT INTO runs (team_a, team_b, started, finished) VALUES " + 
								"(?, ?, now(), 2)");
						stmt.setString(1, team_a);
						stmt.setString(2, team_b);
						db.update(stmt, true);
					}
				}
				rs.close();
				// If idling for a LONG time this could grow and eat up memory
				// so we periodically clear it.
				messageQueue.clear();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					_log.log(Level.WARNING, "Interrupted exception", e);
				}
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, "Error running backend", e);
		}
	}

}
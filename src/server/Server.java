package server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Logger;

import networking.Controller;
import networking.Network;
import networking.Packet;
import networking.PacketType;

import common.Config;

/**
 * Handles distribution of load to connected Clients
 * @author steven
 *
 */
public class Server implements Controller{
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
	private Connection conn;
	private String[] maps;
	private static String[] createStmts = {"CREATE TABLE IF NOT EXISTS `connections` (`id` int(11) NOT NULL AUTO_INCREMENT, `addr` varchar(60) DEFAULT NULL, PRIMARY KEY (`id`))",
		"CREATE TABLE IF NOT EXISTS `delete_queue` (`id` int(11) NOT NULL",
		"CREATE TABLE IF NOT EXISTS `matches` (`id` int(11) NOT NULL DEFAULT '0',`map` varchar(30) NOT NULL, `win` int(1) NOT NULL,`file` varchar(150) NOT NULL)",
		"CREATE TABLE IF NOT EXISTS `queue` (`id` int(11) NOT NULL AUTO_INCREMENT,`team_a` varchar(45) DEFAULT NULL,`team_b` varchar(45) DEFAULT NULL,PRIMARY KEY (`id`))",
		"CREATE TABLE IF NOT EXISTS `running_matches` (`conn_id` int(11) NOT NULL,`map` varchar(50) NOT NULL,`modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
		"CREATE TABLE IF NOT EXISTS `runs` (`id` int(11) NOT NULL AUTO_INCREMENT,`team_a` varchar(45) NOT NULL,`team_b` varchar(45) NOT NULL,`started` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',`finished` int(11) DEFAULT '0',`ended` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,PRIMARY KEY (`id`))",
		"CREATE TABLE IF NOT EXISTS `tags` (`tag` varchar(45) NOT NULL,`alias` varchar(20) DEFAULT NULL,UNIQUE KEY `tag` (`tag`))"
		};

	public Server(Config config) throws Exception {
		this.config = config;
		_log = config.getServerLogger();
		handler = new NetworkHandler(this, config);
		new Thread(handler).start();
		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection(config.db_addr,config.db_user,config.db_pass);
		} catch (Exception e) {
			_log.severe("Cannot connect to database");
			throw e;
		}
		for (String stmt: createStmts) {
			executeSQL(stmt);
		}
	}

	/**
	 * Choose a map and send this client a message to run a match on it
	 * @param net
	 * @param team_a
	 * @param team_b
	 */
	private void sendMatchToClient(Network net, String team_a, String team_b) {
		String map = queued.poll();
		if (map != null) {
			_log.info("Sending map: " + map + " to " + net);
			net.send(new Packet(PacketType.SEND_MAP, map, team_a, team_b, false, null));
			clients.get(net).add(map);
			availableClients.remove(net);
			executeSQL("INSERT INTO running_matches (conn_id, map) VALUES (" + idFromAddr(net.toSQL()) + ", \"" + map + "\")");
		}
	}
	
	/**
	 * Get the connection id of an address
	 * @param addr
	 * @return
	 */
	private int idFromAddr(String addr) {
		int id = 0;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT id FROM connections WHERE addr LIKE \"" + addr + "\"");
			if (rs.first()) {
				id = rs.getInt("id");
			}
		} catch (SQLException e) {
			if (config.DEBUG)
				e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return id;
	}

	/**
	 * Save the results of a match to a file and the database
	 * @param m
	 * @param runid
	 */
	private void saveMatchResults(final Message m, int runid) {
		clients.get(m.receivedBy).remove(m.map);
		executeSQL("DELETE FROM running_matches WHERE map LIKE \"" + m.map + "\"");
		
		if (m.match == null) {
			_log.info(m.map + " did not finish correctly");
			queued.add(m.map);
			return;
		} else {
			try {
				String file = write(m.match, m.map, runid);
				if (!executeSQL("INSERT INTO matches (id, map, win, file) VALUES " + 
						"(" + runid + ", \"" + 
						m.map + "\", " + 
						(m.a_wins ? 1 : 0) + 
						", \"" + file + "\")")) {
					queued.add(m.map);
				}
			} catch (IOException e) {
				_log.severe("Error writing map " + m.map + " to file");
				if (config.DEBUG)
					e.printStackTrace();
				queued.add(m.map);
			}
		}
	}

	/**
	 * Writes a match to a file and returns the filename
	 * @param match Raw data to write to the file
	 * @param map Name of map this was run on
	 * @param runid Run ID of the match
	 * @return The String representing the absolute filename
	 * @throws IOException
	 */
	private String write(byte[] match, String map, int runid) throws IOException {
		String filename = config.matches + "/" + runid + map + ".rms";
		FileOutputStream fo = new FileOutputStream(filename);
		BufferedOutputStream bo = new BufferedOutputStream(fo);
		bo.write(match);
		bo.close();
		_log.info("Finished " + map);
		return filename;
	}

	/**
	 * Check for newly connected clients and add them to our pool
	 */
	private void addClients() {
		Network net = handler.getNewClient();
		if (net != null) {
			_log.info("Adding a new client: " + net);
			clients.put(net, new HashSet<String>());
			executeSQL("INSERT INTO connections (addr) VALUES (\"" + net.toSQL() + "\")");
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
	 */
	private void cleanDeadClients() {
		ArrayList<Network> deadClients = new ArrayList<Network>();
		for (Network network: clients.keySet()) {
			if (!network.isConnected()) {
				deadClients.add(network);
				continue;
			}
		}
		for (Network net: deadClients) {
			executeSQL("DELETE FROM running_matches WHERE conn_id = " + idFromAddr(net.toSQL()));
			executeSQL("DELETE FROM connections WHERE addr LIKE \"" + net.toSQL() + "\"");
			for (String map: clients.get(net))
				queued.add(map);
			clients.remove(net);
			net.stop();
		}
		deadClients.clear();
	}
	
	/**
	 * Check to see if any clients are taking too long to run a match
	 */
	private void checkMatchTimeout() {
		ArrayList<String> timedOut = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT map, TIME_TO_SEC(TIMEDIFF(now(), modified)) AS diff FROM connections c " +
					"JOIN running_matches r ON c.id = r.conn_id");
			while (rs.next()) {
				int diff = rs.getInt("diff");
				if (diff > config.timeout + 10) { // add 10 to allow for network latency
					timedOut.add(rs.getString("map"));
				}
			}
		} catch (SQLException e) {
			if (config.DEBUG)
				e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		for (String map: timedOut) {
			_log.warning("Client timed out on map " + map);
			for (HashSet<String> maps: clients.values()) {
				if (maps.remove(map))
					break;
			}
			queued.add(map);
			executeSQL("DELETE FROM running_matches WHERE map LIKE \"" + map + "\"");
		}
		
	}

	@Override
	public void addPacket(Network network, Packet p) {
		Message m = new Message(p, network);
		m.receivedBy = network;
		messageQueue.add(m);
	}

	/**
	 * Stop everything nicely
	 */
	private void stop() {
		handler.stop();
		for (Network net: clients.keySet()) {
			net.stop();
		}
	}

	/**
	 * Delete all files and database data associated with a run
	 * @param runid
	 */
	private void deleteRun(int runid) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT file FROM matches WHERE id = " + runid);
			while (rs.next()) {
				File file = new File(rs.getString("file"));
				file.delete();
			}
			stmt.close();
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM matches WHERE id = " + runid);
			stmt.close();
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM runs WHERE id = " + runid);
		} catch (SQLException e) {
			_log.severe("Error deleting run");
			if (config.DEBUG)
				e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	private void clearUnfinishedRuns() {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT id FROM runs WHERE finished = 0");
			while (rs.next()) {
				int runid = rs.getInt("id");
				_log.info("Clearing out unfinished run: " + runid);
				deleteRun(runid);
			}
		} catch (SQLException e) {
			_log.severe("Error clearing out unfinished runs");
			if (config.DEBUG)
				e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		checkUserDeleteRuns(-1);
	}

	/**
	 * Delete past runs when requested
	 * @param currentRunid
	 * @return True if the deleted run is not the current run
	 */
	private boolean checkUserDeleteRuns(int currentRunid) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT id FROM delete_queue");
			ArrayList<Integer> deleted_runs = new ArrayList<Integer>();
			while (rs.next()) {
				int deleteID = rs.getInt("id");
				deleteRun(deleteID);
				deleted_runs.add(deleteID);
				if (deleteID == currentRunid) {
					for (int run: deleted_runs) {
						stmt.close();
						stmt = conn.createStatement();
						stmt.execute("DELETE FROM delete_queue WHERE id = " + run);
					}
					return false;
				}
			}
			for (int run: deleted_runs) {
				stmt.close();
				stmt = conn.createStatement();
				stmt.execute("DELETE FROM delete_queue WHERE id = " + run);
			}

		} catch (SQLException e) {
			_log.severe("Error managing the delete_queue");
			if (config.DEBUG)
				e.printStackTrace();
		}  finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return true;
	}

	private void removeFromQueue(int queueID) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM queue WHERE id = " + queueID);
		} catch (SQLException e) {
			_log.severe("Error removing run from queue");
			if (config.DEBUG)
				e.printStackTrace();
		}  finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	private void handlePackets(int runid, String team_a, String team_b) {
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

	private void resetRun() {
		queued.clear();
		availableClients.clear();
		// Tell all clients to stop any current runs they may have
		for (Network net: clients.keySet()) {
			net.send(new Packet(PacketType.RESET_RUNS, null, null, null, true, null));
		}
		executeSQL("DELETE FROM connections");
		executeSQL("DELETE FROM running_matches");
		for (Network net: clients.keySet())
			executeSQL("INSERT INTO connections (addr) VALUES (\"" + net.toSQL() + "\")");
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

	private void runMatch(String team_a, String team_b) {
		_log.info("Running " + team_a + " vs " + team_b);
		resetRun();
		File file = new File("maps");
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
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if (!executeSQL("INSERT INTO runs (team_a, team_b, started) VALUES " + 
					"(\"" + team_a + "\", \"" + team_b + "\", now())")) {
				// An exception was thrown, exit
				return;
			}
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT MAX(id) max FROM runs");
			if (rs.first())
				runid = rs.getInt("max");
			else {
				_log.severe("Could not find max value in table 'runs'");
				return;
			}

			while (checkUserDeleteRuns(runid) && !(queued.isEmpty() && getNumMatchesCurrentlyRunning() == 0)) {
				addClients();
				handlePackets(runid, team_a, team_b);
				cleanDeadClients();
				checkMatchTimeout();
				Network idleClient = getIdleClient();
				if (idleClient != null) {
					sendMatchToClient(idleClient, team_a, team_b);
				}
					
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					_log.warning("Interrupt exception");
				}
			}
			if (!executeSQL("UPDATE runs SET finished = 1 WHERE id = " + runid)) {
				_log.severe("Error setting run as finished");
			}
			_log.info("Finished running " + team_a + " vs " + team_b);
			resetRun();
		} catch (SQLException e) {
			_log.severe("Error managing database");
			if (config.DEBUG)
				e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * Executes a SQL statement on the database
	 * @param sql
	 * @return True if the statement succeeded, false if an exception was thrown
	 */
	private boolean executeSQL(String sql) {
		boolean succeeded = true;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			succeeded = false;
			_log.warning("Failed to execute SQL: " + sql);
			if (config.DEBUG)
				e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
		return succeeded;
	}

	private String[] validateTeams(String team_a, String team_b) {
		Statement stmt = null;
		ResultSet rs = null;
		String[] teams = new String[2];
		HashSet<String> tags = new HashSet<String>();
		HashMap<String, String> aliases = new HashMap<String, String>();
		try {
			stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT tag, alias FROM tags");
				while (rs.next()) {
					String tag = rs.getString("tag");
					String alias = rs.getString("alias");
					tags.add(tag);
					aliases.put(alias, tag);
				} 
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
		} catch (Exception e) {
			_log.severe("Error reading from database");
			if (config.DEBUG)
				e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}

	public void run() {
		boolean noErrors = true;
		executeSQL("DELETE FROM connections");
		executeSQL("DELETE FROM running_matches");
		while (noErrors) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				clearUnfinishedRuns();
				addClients();
				cleanDeadClients();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT id, team_a, team_b FROM queue ORDER BY id LIMIT 1");
				int id = 0;
				if (rs.first()) {
					Process p = Runtime.getRuntime().exec(new String[] {config.cmd_update, "server"});
					p.waitFor();
					String team_a = rs.getString("team_a");
					String team_b = rs.getString("team_b");
					id = rs.getInt("id");
					removeFromQueue(id);
					String[] teamNames = validateTeams(team_a, team_b);
					if (teamNames != null) {
						runMatch(teamNames[0], teamNames[1]);
					}
					stmt.close();
					rs.close();
				}
				// If idling for a LONG time this could grow and eat up memory
				// so we periodically clear it.
				messageQueue.clear();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					if (config.DEBUG)
						e.printStackTrace();
				}

			} catch (Exception e) {
				_log.severe("Error reading from database");
				if (config.DEBUG)
					e.printStackTrace();
				noErrors = false;

			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
					}
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
					}
				}
			}
		}
		stop();
	}

}
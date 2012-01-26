package master;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import model.BSScrimmageSet;
import networking.Controller;
import networking.Dependencies;
import networking.DependencyHashes;
import networking.Network;
import networking.Packet;
import networking.PacketCmd;

import org.apache.log4j.Logger;

import common.NetworkMatch;

/**
 * Representation of a worker that is used by the server
 * @author stevearc
 *
 */
public class WorkerRepr implements Controller {
	private static Logger _log = Logger.getLogger(WorkerRepr.class);
	private int id;
	private Network net;
	private HashSet<NetworkMatch> runningMatches;
	private HashSet<BSScrimmageSet> analyzingMatches;

	public WorkerRepr(Socket s, int id) throws IOException {
		this.id = id;
		this.net = new Network(this, s);
		runningMatches = new HashSet<NetworkMatch>();
		analyzingMatches = new HashSet<BSScrimmageSet>();
	}
	
	public int getId() {
		return id;
	}

	/**
	 * Messages worker telling it to run a Match
	 * @param m The Match to run
	 */
	public synchronized void runMatch(NetworkMatch m, DependencyHashes deps) {
		runningMatches.add(m);
		Packet p = new Packet(PacketCmd.RUN, new Object[] {m, deps});
		net.send(p);
	}
	
	public synchronized void analyzeMatch(BSScrimmageSet scrim, byte[] fileData, DependencyHashes deps) {
		analyzingMatches.add(scrim);
		Packet p = new Packet(PacketCmd.ANALYZE, new Object[] {scrim, fileData, deps});
		net.send(p);
	}
	
	public synchronized void sendDependencies(Dependencies dep) {
		Packet p = new Packet(PacketCmd.DEPENDENCIES, new Object[] {dep});
		net.send(p);
	}
	
	public synchronized void restart() {
		Packet p = new Packet(PacketCmd.RESTART, new Object[0]);
		net.send(p);
	}
	
	/**
	 * Messages worker telling it to stop the matches it's running
	 */
	public synchronized void stopAllMatches() {
		Packet p = new Packet(PacketCmd.STOP, new Object[] {});
		net.send(p);
		runningMatches.clear();
	}

	/**
	 * Start running the networking thread
	 */
	public void start() {
		new Thread(net).start();
	}

	/**
	 * Close the connection and stop running processes
	 */
	public synchronized void stop() {
		net.close();
	}

	/**
	 * 
	 * @return Set of all Matches currently being run on the worker
	 */
	public synchronized Set<NetworkMatch> getRunningMatches() {
		return runningMatches;
	}
	
	public synchronized Set<BSScrimmageSet> getAnalyzingMatches() {
		return analyzingMatches;
	}

	@Override
	public synchronized void addPacket(Packet p) {
		switch (p.getCmd()) {
		case RUN_REPLY:
			runningMatches.remove((NetworkMatch) p.get(0));
			AbstractMaster.getMaster().matchFinished(this, p);
			break;
		case REQUEST_MATCH:
			AbstractMaster.getMaster().sendWorkerMatches(this);
			break;
		case REQUEST_DEPENDENCIES:
			NetworkMatch match = (NetworkMatch) p.get(0);
			boolean needUpdateBsTester = (Boolean) p.get(1);
			boolean needUpdate = (Boolean) p.get(2);
			boolean needMap = (Boolean) p.get(3);
			boolean needTeamA = (Boolean) p.get(4);
			boolean needTeamB = (Boolean) p.get(5);
			AbstractMaster.getMaster().sendWorkerDependencies(this, match, needUpdateBsTester, needUpdate, needMap, needTeamA, needTeamB);
			break;
		case ANALYZE_REPLY:
			analyzingMatches.remove((BSScrimmageSet)p.get(0));
			AbstractMaster.getMaster().matchAnalyzed(this, p);
			break;
		default:
			_log.warn("Invalid packet command: " + p.getCmd());
		}
	}

	@Override
	public String toString() {
		return id + ": " + net.toString();
	}

	/**
	 * Formats the WorkerRepr for display on the web server
	 * @return
	 */
	public String toHTML() {
		String[] addrs = net.toString().split("/");
			return "<span id='" + id + "'>" + addrs[1] + "</span>";
	}

	@Override
	public void onDisconnect() {
		AbstractMaster.kickoffWorkerDisconnect(this);
	}

}

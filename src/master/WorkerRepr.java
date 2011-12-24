package master;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import networking.Controller;
import networking.Network;
import networking.Packet;
import networking.PacketCmd;

import org.apache.log4j.Logger;

import common.Dependencies;
import common.NetworkMatch;

/**
 * Representation of a worker that is used by the server
 * @author stevearc
 *
 */
public class WorkerRepr implements Controller {
	private static Logger _log = Logger.getLogger(WorkerRepr.class);
	private Network net;
	private HashSet<NetworkMatch> runningMatches;
	private int numCores = 0;

	public WorkerRepr(Socket s) throws IOException {
		this.net = new Network(this, s);
		runningMatches = new HashSet<NetworkMatch>();
	}

	/**
	 * Messages worker telling it to run a Match
	 * @param m The Match to run
	 */
	public synchronized void runMatch(NetworkMatch m) {
		runningMatches.add(m);
		Packet p = new Packet(PacketCmd.RUN, new Object[] {m, null});
		net.send(p);
	}
	
	public synchronized void runMatchWithDependencies(NetworkMatch m, Dependencies dep) {
		// Don't add it to runningMatches because this method is only ever called
		// if we have ALREADY called runMatch()
		Packet p = new Packet(PacketCmd.RUN, new Object[] {m, dep});
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
	 * 
	 * @return True if worker can accept more matches
	 */
	public boolean isFree() {
		return runningMatches.size() < numCores;
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

	@Override
	public synchronized void addPacket(Packet p) {
		switch (p.getCmd()) {
		case RUN_REPLY:
			runningMatches.remove((NetworkMatch) p.get(0));
			AbstractMaster.kickoffMatchFinished(this, p);
			break;
		case INIT:
			numCores = (Integer) p.get(0);
			AbstractMaster.kickoffSendWorkerMatches(this);
			break;
		case REQUEST:
			NetworkMatch match = (NetworkMatch) p.get(0);
			boolean needUpdate = (Boolean) p.get(1);
			boolean needMap = (Boolean) p.get(2);
			boolean needTeamA = (Boolean) p.get(3);
			boolean needTeamB = (Boolean) p.get(4);
			AbstractMaster.kickoffSendWorkerMatchDependencies(this, match, needUpdate, needMap, needTeamA, needTeamB);
			break;
		default:
			_log.warn("Invalid packet command: " + p.getCmd());
		}
	}

	@Override
	public String toString() {
		return net.toString();// + ": " + runningMatches;
	}

	/**
	 * Formats the WorkerRepr for display on the web server
	 * @return
	 */
	public String toHTML() {
		String[] addrs = net.toString().split("/");
			return addrs[1];
	}

	@Override
	public void onDisconnect() {
		AbstractMaster.kickoffWorkerDisconnect(this);
	}

}

package backend;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

import networking.Controller;
import networking.Network;
import networking.Packet;
import networking.PacketCmd;

import common.Config;
import common.Match;

public class ClientRepr implements Controller {
	private Config config;
	private Logger _log;
	private Network net;
	private ArrayList<Match> runningMatches;
	private int numCores = 0;

	public ClientRepr(Socket s) throws IOException {
		config = Config.getConfig();
		_log = config.getLogger();
		this.net = new Network(this, s);
		runningMatches = new ArrayList<Match>();
	}

	public synchronized void runMatch(Match m) {
		runningMatches.add(m);
		Packet p = new Packet(PacketCmd.RUN, new Object[] {m});
		net.send(p);
	}

	public synchronized void stopAllMatches() {
		Packet p = new Packet(PacketCmd.STOP, new Object[] {});
		net.send(p);
		runningMatches.clear();
	}

	public boolean isFree() {
		return runningMatches.size() < numCores;
	}

	public void start() {
		new Thread(net).start();
	}

	public synchronized void stop() {
		net.close();
	}

	public synchronized ArrayList<Match> getRunningMatches() {
		return runningMatches;
	}

	@Override
	public synchronized void addPacket(Packet p) {
		switch (p.getCmd()) {
		case RUN_REPLY:
			runningMatches.remove((Match) p.get(0));
			ServerMethodCaller.matchFinished(this, p);
			break;
		case INIT:
			numCores = (Integer) p.get(0);
			ServerMethodCaller.sendClientMatches(this);
			break;
		default:
			_log.warning("Invalid packet command: " + p.getCmd());
		}
	}

	@Override
	public String toString() {
		return net.toString() + ": " + runningMatches;
	}

	public String toHTML() {
		String[] addrs = net.toString().split("/");
		if (addrs[0].trim().equals(""))
			return "<a>" + addrs[1] + "</a>";
		else
			return "<a title='" + addrs[1] + "'>" + addrs[0] + "</a>";
	}

	@Override
	public void onDisconnect() {
		Config.getServer().clientDisconnect(this);
	}

}

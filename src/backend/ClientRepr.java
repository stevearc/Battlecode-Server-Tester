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
	private boolean authenticated = false;
	private int numCores = 1;

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

	public boolean isAuthenticated() {
		return authenticated;
	}

	public boolean isFree() {
		if (!authenticated)
			return false;
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
		case AUTH:
			if (config.password.equals(p.get(0))) {
				numCores = (Integer) p.get(1);
				net.send(new Packet(PacketCmd.AUTH_REPLY, new Object[] {"ok"}));
				authenticated = true;
				ServerMethodCaller.sendClientMatches(this);
			} else {
				net.send(new Packet(PacketCmd.AUTH_REPLY, new Object[] {"password mismatch"}));
			}
			if (!authenticated) {
				net.close();
			}
			break;
		default:
			_log.warning("Invalid packet command: " + p.getCmd());
		}
	}

	@Override
	public String toString() {
		return net.toString() + ": " + runningMatches;
	}

	public String connectionString() {
		return net.toString();
	}

	@Override
	public void onDisconnect() {
		Config.getServer().clientDisconnect(this);
	}

}

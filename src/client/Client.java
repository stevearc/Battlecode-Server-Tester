package client;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import networking.Controller;
import networking.Network;
import networking.Packet;
import networking.PacketType;

import common.Config;

/**
 * Handles connecting to server and running matches
 * @author steven
 *
 */
public class Client implements Controller, Runnable {
	private Logger _log;
	private Network network;
	private Vector<Packet> packetQueue = new Vector<Packet>();
	private HashSet<MatchRunner> running = new HashSet<MatchRunner>();
	private Config config;
	private ReentrantLock repoLock = new ReentrantLock();

	public Client(Config config) {
		this.config = config;
		_log = config.getClientLogger();
	}

	/**
	 * Main method.  Starts everything.
	 */
	public void run() {
		while (true) {
			// Try to connect to the server
			Socket socket = null;
			try {
				socket = new Socket(config.server, config.port);
				network = new Network(this, config, socket, _log);
				new Thread(network).start();
				_log.info("Connected to server");
			} catch (IOException e) {
				_log.warning("Could not connect to " + config.server + " on port " + config.port);
				if (config.DEBUG)
					e.printStackTrace();
			}

			while (network != null && network.isConnected()) {
				// Check to see if any matches finished
				HashSet<MatchRunner> finished = new HashSet<MatchRunner>();
				for (MatchRunner mr: running) {
					Packet p = mr.getResponse();
					if (p != null) {
						network.send(p);
						finished.add(mr);
					}
				}
				running.removeAll(finished);

				// Check to see if we have any new packets
				if (packetQueue.size() != 0) {
					while (packetQueue.size() > 1) {
						packetQueue.remove(0);
					}
					Packet p = packetQueue.firstElement();
					packetQueue.remove(0);
					switch (p.type) {
					case MAP_RESULT:
						// Not handled by Client
						break;
					case REQUEST_MAP:
						// Not handled by Client
						break;
					case RESET_RUNS:
						// Stop all currently running matches
						for (MatchRunner mr: running) {
							_log.info("Terminating current run");
							mr.stop();
						}
						running.clear();
						break;
					case SEND_MAP:
						// Run the match described by the packet
						if (running.size() < config.cores) {
							MatchRunner runner = new MatchRunner(config, p, repoLock);
							running.add(runner);
							new Thread(runner).start();
						} // Otherwise we can't handle it
						else {
							network.send(new Packet(PacketType.MAP_RESULT, p.map, p.team_a, p.team_b, 0, true, null));
						}
						break;
					}

				} // If we can run more matches, request more
				else if (running.size() < config.cores){
					network.send(new Packet(PacketType.REQUEST_MAP, null, null, null, 0, false, null));
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					if (config.DEBUG)
						e.printStackTrace();
				}
			}
			
			// Clean up any matches still running
			for (MatchRunner mr: running) {
				_log.info("Terminating current run");
				mr.stop();
			}
			network = null;

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				if (config.DEBUG)
					e.printStackTrace();
			}
		}
	}

	@Override
	public void addPacket(Network network, Packet p) {
		packetQueue.add(p);
	}
}

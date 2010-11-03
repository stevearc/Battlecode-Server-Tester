package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import networking.Controller;
import networking.Network;
import networking.Packet;
import networking.PacketCmd;

import common.Config;
import common.Match;

/**
 * Handles connecting to server and running matches
 * @author steven
 *
 */
public class Client implements Controller, Runnable {
	private Config config;
	private Logger _log;
	private Network network;
	private HashSet<MatchRunner> running = new HashSet<MatchRunner>();
	private ReentrantLock repoLock = new ReentrantLock();
	private boolean runClient = true;
	private SSLSocketFactory sf;

	public Client() throws Exception{
		config = Config.getConfig();
		_log = config.getLogger();

		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(new FileInputStream(config.keystore), config.keystore_pass.toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(keystore);

		SSLContext context = SSLContext.getInstance("TLS");
		TrustManager[] trustManagers = tmf.getTrustManagers();
		
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(keystore, config.keystore_pass.toCharArray());
		KeyManager[] keyManagers = kmf.getKeyManagers();

		context.init(keyManagers, trustManagers, null);
		sf = context.getSocketFactory();
	}

	public synchronized void matchFinish(MatchRunner mr, Match match, String status, int winner, byte[] data) {
		Packet p = new Packet(PacketCmd.RUN_REPLY, new Object[] {match, status, winner, data});
		network.send(p);
		mr.stop();
		running.remove(mr);
	}

	@Override
	public void run() {
		while (runClient) {
			try {
				if (network == null || !network.isConnected()) {
					try {
						Socket socket = sf.createSocket(config.server, config.port);
						network = new Network(this, socket);
						new Thread(network).start();
						_log.info("Connecting to server");
						network.send(new Packet(PacketCmd.INIT, new Object[]{config.cores}));
					} catch (UnknownHostException e) {
					} catch (IOException e) {
					}
				}
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				_log.log(Level.WARNING, "Interrupt exception", e);
			}
		}
	}

	@Override
	public synchronized void addPacket(Packet p) {
		switch (p.getCmd()) {
		case RUN:
			try {
				MatchRunner m = new MatchRunner(this, (Match) p.get(0), repoLock);
				new Thread(m).start();
				running.add(m);
			} catch (Exception e) {
				_log.warning("Error running match");
			}
			break;
		case STOP:
			_log.info("Received stop command");
			for (MatchRunner mr: running) {
				mr.stop();
			}
			running.clear();
			break;
		default:
			_log.warning("Unrecognized packet command: " + p.getCmd());
		}
	}

	@Override
	public synchronized void onDisconnect() {
		for (MatchRunner mr: running) {
			mr.stop();
		}
		running.clear();
		_log.info("Disconnected from server");
	}
}

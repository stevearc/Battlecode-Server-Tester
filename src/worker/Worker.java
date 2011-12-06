package worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import model.BSMap;
import networking.Controller;
import networking.Network;
import networking.Packet;
import networking.PacketCmd;

import common.Config;
import common.Dependencies;
import common.NetworkMatch;

/**
 * Connects to master and runs matches
 * @author stevearc
 *
 */
public class Worker implements Controller, Runnable {
	private Config config;
	private Logger _log;
	private Network network;
	private MatchRunner[] running;
	private boolean runWorker = true;
	private SSLSocketFactory sf;

	public Worker() throws Exception{
		config = Config.getConfig();
		_log = config.getLogger();
		running = new MatchRunner[config.cores];

		// Connect to the master using the certificate in the keystore
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

	/**
	 * Send match data to master
	 * @param mr
	 * @param match
	 * @param status
	 * @param winner
	 * @param win_condition
	 * @param a_points
	 * @param b_points
	 * @param data
	 */
	public synchronized void matchFinish(int core, NetworkMatch match, String status, int winner, 
			int win_condition, double a_points, double b_points, byte[] data) {
		Packet p = new Packet(PacketCmd.RUN_REPLY, new Object[] {match, status, winner, win_condition, a_points, b_points, data});
		network.send(p);
		running[core].stop();
		running[core] = null;
	}

	@Override
	public void run() {
		while (runWorker) {
			try {
				if (network == null || !network.isConnected()) {
					try {
						Socket socket = sf.createSocket(config.server, config.port);
						network = new Network(this, socket);
						new Thread(network).start();
						_log.info("Connecting to master");
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

	private boolean haveMap(BSMap map) {
		File mapFile = new File(config.install_dir + "/battlecode/maps/" + map.getMapName() + ".xml");
		return mapFile.exists();
	}

	private boolean havePlayer(String player) {
		File playerFile = new File(config.install_dir + "/battlecode/teams/" + player + ".jar");
		return playerFile.exists();
	}

	/**
	 * Checks to make sure we have the right files.  Requests them if not.
	 * @param match
	 * @return
	 */
	private boolean resolveDependencies(NetworkMatch match) {
		boolean allClear = true;
		boolean needMap = false;
		boolean needTeamA = false;
		boolean needTeamB = false;
		if (!haveMap(match.map)) {
			allClear = false;
			needMap = true;
		}
		if (!havePlayer(match.team_a)) {
			allClear = false;
			needTeamA = true;
		}
		if (!havePlayer(match.team_b)) {
			allClear = false;
			needTeamB = true;
		}
		if (!allClear) {
			_log.info("Requesting " + (needMap ? "map: " + match.map + " " : "") + 
					(needTeamA ? "player: " + match.team_a + " " : "") + 
					(needTeamB ? "player: " + match.team_b + " " : ""));
			Packet requestPacket = new Packet(PacketCmd.REQUEST, new Object[]{match, needMap, needTeamA, needTeamB});
			network.send(requestPacket);
		}
		return allClear;
	}
	
	private boolean fileEqualsData(File file, byte[] data) throws IOException {
		if (file.length() != data.length) {
			return false;
		}
		byte[] fileData = new byte[data.length];
		FileInputStream fis = new FileInputStream(file);
		fis.read(fileData);
		return Arrays.equals(fileData, data);
	}

	private void writeDependencies(Dependencies dep) {
		if (dep == null) {
			return;
		}
		String fileName;
		File outputFile;
		FileOutputStream ostream;
		try {
			if (dep.map != null) {
				fileName = config.install_dir + "/battlecode/maps/" + dep.mapName + ".xml";
				_log.info("Writing file: " + fileName);
				outputFile = new File(fileName);
				if (outputFile.exists()) {
					if (!fileEqualsData(outputFile, dep.map)){
						outputFile.delete();
					}
				}
				ostream = new FileOutputStream(fileName);
				ostream.write(dep.map);
				ostream.close();
			}
			if (dep.teamA != null) {
				fileName = config.install_dir + "/battlecode/teams/" + dep.teamAName + ".jar";
				_log.info("Writing file: " + fileName);
				outputFile = new File(fileName);
				if (outputFile.exists()) {
					if (!fileEqualsData(outputFile, dep.teamA)) {
						outputFile.delete();
					}
				}
				ostream = new FileOutputStream(fileName);
				ostream.write(dep.teamA);
				ostream.close();
			}
			if (dep.teamB != null) {
				fileName = config.install_dir + "/battlecode/teams/" + dep.teamBName + ".jar";
				_log.info("Writing file: " + fileName);
				outputFile = new File(fileName);
				if (outputFile.exists()) {
					if (!fileEqualsData(outputFile, dep.teamB)) {
						outputFile.delete();
					}
				}
				ostream = new FileOutputStream(fileName);
				ostream.write(dep.teamB);
				ostream.close();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Could not create player or map file", e);
		}
	}

	@Override
	public synchronized void addPacket(Packet p) {
		switch (p.getCmd()) {
		case RUN:
			try {
				// Find a free core
				int core;
				for (core = 0; core < config.cores; core++) {
					if (running[core] == null)
						break;
				}
				// Could not find free core
				if (core == config.cores)
					break;

				NetworkMatch match = (NetworkMatch) p.get(0);
				Dependencies dep = (Dependencies) p.get(1);
				writeDependencies(dep);
				if (resolveDependencies(match)) {
					MatchRunner m = new MatchRunner(this, (NetworkMatch) p.get(0), core);
					new Thread(m).start();
					running[core] = m;
				}
			} catch (Exception e) {
				_log.warning("Error running match");
			}
			break;
		case STOP:
			_log.info("Received stop command");
			for (int i = 0; i < config.cores; i++) {
				if (running[i] != null) {
					running[i].stop();
					running[i] = null;
				}
			}
			break;
		default:
			_log.warning("Unrecognized packet command: " + p.getCmd());
		}
	}

	@Override
	public synchronized void onDisconnect() {
		for (int i = 0; i < config.cores; i++) {
			if (running[i] != null) {
				running[i].stop();
				running[i] = null;
			}
		}
		_log.info("Disconnected from master");
	}
}

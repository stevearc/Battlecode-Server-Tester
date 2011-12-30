package worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.SocketFactory;

import model.BSMap;
import model.BSMatch;
import model.MatchResult;
import networking.Controller;
import networking.Network;
import networking.Packet;
import networking.PacketCmd;

import org.apache.log4j.Logger;

import common.Config;
import common.Dependencies;
import common.NetworkMatch;
import common.Util;

/**
 * Connects to master and runs matches
 * @author stevearc
 *
 */
public class Worker implements Controller, Runnable {
	private Logger _log = Logger.getLogger(Worker.class);
	private String serverAddr;
	private Network network;
	private int cores;
	private int dataPort;
	private MatchRunner[] running;
	private boolean runWorker = true;
	private SocketFactory sf;

	public Worker(String serverAddr, int dataPort, int cores) throws Exception{
		this.serverAddr = serverAddr;
		this.dataPort = dataPort;
		this.cores = cores;
		running = new MatchRunner[cores];
		sf = SocketFactory.getDefault();
	}
	
	public synchronized void matchFailed(MatchRunner runner, int core, NetworkMatch match) {
		matchFinish(runner, core, match, null, null, null);
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
	public synchronized void matchFinish(MatchRunner runner, int core, NetworkMatch match, BSMatch.STATUS status, MatchResult result, byte[] data) {
		// If the runner is out of date, we should ignore it
		if (running[core] != runner) {
			return;
		}
		Packet p = new Packet(PacketCmd.RUN_REPLY, new Object[] {match, status, result, data});
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
						Socket socket = sf.createSocket(serverAddr, dataPort);
						network = new Network(this, socket);
						new Thread(network).start();
						_log.info("Connecting to master");
						network.send(new Packet(PacketCmd.INIT, new Object[]{cores}));
					} catch (UnknownHostException e) {
					} catch (IOException e) {
					}
				}
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				_log.warn("Interrupt exception", e);
			}
		}
	}

	private boolean battlecodeUpToDate(NetworkMatch nm) {
		try {
			if (!nm.battlecodeServerHash.equals(Util.convertToHex(Util.SHA1Checksum("./lib/battlecode-server.jar")))) {
				return false;
			}
			if (!nm.idataHash.equals(Util.convertToHex(Util.SHA1Checksum("./idata")))) {
				return false;
			}
			if (!nm.buildHash.equals(Util.convertToHex(Util.SHA1Checksum("./build.xml")))) {
				return false;
			}
			if (!nm.confHash.equals(Util.convertToHex(Util.SHA1Checksum("./bc.conf")))) {
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			_log.error("Could not find SHA1 algorithm", e);
			return false;
		} catch (FileNotFoundException e) {
			// If the files don't exist, we obviously need to update them
			return false;
		} catch (IOException e) {
			_log.error("Error hashing battlecode install files", e);
			return false;
		}
		return true;
	}

	private boolean haveMap(BSMap map) {
		File mapFile = new File("./maps/" + map.getMapName() + ".xml");
		if (mapFile.exists()) {
			try {
				return map.getHash().equals(Util.convertToHex(Util.SHA1Checksum(mapFile.getAbsolutePath())));
			} catch (NoSuchAlgorithmException e) {
				_log.error("Can't find SHA1 hash!", e);
			} catch (IOException e) {
				_log.error("Can't read map file!", e);
			}
		}
		return false;
	}

	private boolean havePlayer(String player) {
		File playerFile = new File("./teams/" + player + ".jar");
		return playerFile.exists();
	}

	/**
	 * Checks to make sure we have the right files.  Requests them if not.
	 * @param match
	 * @return
	 */
	private boolean resolveDependencies(NetworkMatch match) {
		boolean allClear = true;
		boolean needUpdate = false;
		boolean needMap = false;
		boolean needTeamA = false;
		boolean needTeamB = false;
		if (!battlecodeUpToDate(match)) {
			allClear = false;
			needUpdate = true;
		}
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
			_log.info("Requesting " + (needUpdate ? "battlecode files, " : "") + 
					(needMap ? "map: " + match.map + ", " : "") + 
					(needTeamA ? "player: " + match.team_a + ", " : "") + 
					(needTeamB ? "player: " + match.team_b : ""));
			Packet requestPacket = new Packet(PacketCmd.REQUEST, new Object[]{match, needUpdate, needMap, needTeamA, needTeamB});
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
	
	private void writeDataToFile(byte[] data, String fileName) throws IOException {
		_log.info("Writing file: " + fileName);
		File outputFile = new File(fileName);
		if (outputFile.exists()) {
			if (!fileEqualsData(outputFile, data)){
				outputFile.delete();
			}
		}
		FileOutputStream ostream = new FileOutputStream(fileName);
		ostream.write(data);
		ostream.close();
	}

	private void writeDependencies(Dependencies dep) {
		if (dep == null) {
			return;
		}
		try {
			if (dep.battlecodeServer != null) {
				writeDataToFile(dep.battlecodeServer, "./lib/battlecode-server.jar");
			}
			if (dep.idata != null) {
				writeDataToFile(dep.idata, "./idata");
			}
			if (dep.build != null) {
				writeDataToFile(dep.build, "./build.xml");
			}
			if (dep.bc_conf != null) {
				writeDataToFile(dep.bc_conf, "./bc.conf");
			}
			if (dep.map != null) {
				writeDataToFile(dep.map, "./maps/" + dep.mapName + ".xml");
			}
			if (dep.teamA != null) {
				writeDataToFile(dep.teamA, "./teams/" + dep.teamAName + ".jar");
			}
			if (dep.teamB != null) {
				writeDataToFile(dep.teamB, "./teams/" + dep.teamBName + ".jar");
			}
			
			// If we wrote the battlecode-server.jar file, we need to restart
			if (dep.battlecodeServer != null) {
				_log.info("battlecode-server.jar updated, restarting worker");
				System.exit(Config.RESTART_STATUS);
			}
		} catch (IOException e) {
			_log.error("Could not create player or map file", e);
		}
	}

	@Override
	public synchronized void addPacket(Packet p) {
		switch (p.getCmd()) {
		case RUN:
			try {
				// Find a free core
				int core;
				for (core = 0; core < cores; core++) {
					if (running[core] == null)
						break;
				}
				// Could not find free core
				if (core == cores)
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
				_log.warn("Error running match");
			}
			break;
		case STOP:
			_log.info("Received stop command");
			for (int i = 0; i < cores; i++) {
				if (running[i] != null) {
					running[i].stop();
					running[i] = null;
				}
			}
			break;
		default:
			_log.warn("Unrecognized packet command: " + p.getCmd());
		}
	}

	@Override
	public synchronized void onDisconnect() {
		for (int i = 0; i < cores; i++) {
			if (running[i] != null) {
				running[i].stop();
				running[i] = null;
			}
		}
		_log.info("Disconnected from master");
	}
}

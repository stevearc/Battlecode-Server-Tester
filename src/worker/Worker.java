package worker;

import java.io.File;
import java.io.FileFilter;
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
import model.BSScrimmageSet;
import model.MatchResultImpl;
import model.STATUS;
import networking.Controller;
import networking.Dependencies;
import networking.DependencyHashes;
import networking.Network;
import networking.Packet;
import networking.PacketCmd;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import common.BSUtil;
import common.Config;
import common.NetworkMatch;

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
	public synchronized void matchFinish(MatchRunner runner, int core, NetworkMatch match, STATUS status, MatchResultImpl result, byte[] data) {
		// If the runner is out of date, we should ignore it
		if (running[core] != runner) {
			return;
		}
		Packet p = new Packet(PacketCmd.RUN_REPLY, new Object[] {match, status, result, data});
		network.send(p);
		running[core].stop();
		running[core] = null;
	}

	public synchronized void matchAnalyzed(MatchRunner runner, int core, BSScrimmageSet scrim, STATUS status) {
		// If the runner is out of date, we should ignore it
		if (running[core] != runner) {
			return;
		}
		Packet p = new Packet(PacketCmd.ANALYZE_REPLY, new Object[] {scrim, status});
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

	private boolean battlecodeUpToDate(DependencyHashes deps) {
		if (deps == null) {
			return true;
		}
		try {
			if (!deps.battlecodeServerHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum("lib" + File.separator + "battlecode-server.jar")))) {
				return false;
			}
			if (!deps.allowedPackagesHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum("AllowedPackages.txt")))) {
				return false;
			}
			if (!deps.disallowedClassesHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum("DisallowedClasses.txt")))) {
				return false;
			}
			if (!deps.methodCostsHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum("MethodCosts.txt")))) {
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
		File mapFile = new File("maps" + File.separator + map.getMapName() + ".xml");
		if (mapFile.exists()) {
			try {
				return map.getHash().equals(BSUtil.convertToHex(BSUtil.SHA1Checksum(mapFile.getAbsolutePath())));
			} catch (NoSuchAlgorithmException e) {
				_log.error("Can't find SHA1 hash!", e);
			} catch (IOException e) {
				_log.error("Can't read map file!", e);
			}
		}
		return false;
	}

	private boolean havePlayer(String player) {
		File playerFile = new File("teams" + File.separator + player + ".jar");
		return playerFile.exists();
	}

	/**
	 * Checks to make sure we have the right files.  Requests them if not.
	 * @param match
	 * @return
	 */
	private boolean resolveDependencies(NetworkMatch match, DependencyHashes deps) {
		boolean allClear = true;
		boolean needUpdate = false;
		boolean needMap = false;
		boolean needTeamA = false;
		boolean needTeamB = false;
		if (!battlecodeUpToDate(deps)) {
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
			Packet requestPacket = new Packet(PacketCmd.REQUEST_DEPENDENCIES, new Object[]{match, needUpdate, needMap, needTeamA, needTeamB});
			network.send(requestPacket);
		}
		return allClear;
	}

	private boolean resolveDependencies(DependencyHashes deps) {
		boolean needUpdate = false;
		if (!battlecodeUpToDate(deps)) {
			needUpdate = true;
		}
		if (needUpdate) {
			_log.info("Requesting " + (needUpdate ? "battlecode files" : ""));
			Packet requestPacket = new Packet(PacketCmd.REQUEST_DEPENDENCIES, new Object[]{null, needUpdate, false, false, false});
			network.send(requestPacket);
		}
		return !needUpdate;
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

	/**
	 * Write dependency file data to the file system.  
	 * @param dep
	 * @return true if battlecode-server.jar was updated and we need to restart
	 */
	private boolean writeDependencies(Dependencies dep) {
		if (dep == null) {
			return false;
		}
		boolean needRestart = false;
		try {
			if (dep.battlecodeServer != null) {
				writeDataToFile(dep.battlecodeServer, "lib" + File.separator + "battlecode-server.jar");
				needRestart = true;
			}
			if (dep.allowedPackages != null) {
				writeDataToFile(dep.allowedPackages, "AllowedPackages.txt");
			}
			if (dep.disallowedClasses != null) {
				writeDataToFile(dep.disallowedClasses, "DisallowedClasses.txt");
			}
			if (dep.methodCosts != null) {
				writeDataToFile(dep.methodCosts, "MethodCosts.txt");
			}
			if (dep.map != null) {
				writeDataToFile(dep.map, "maps" + File.separator + dep.mapName + ".xml");
			}
			if (dep.teamA != null) {
				writeDataToFile(dep.teamA, "teams" + File.separator + dep.teamAName + ".jar");
			}
			if (dep.teamB != null) {
				writeDataToFile(dep.teamB, "teams" + File.separator + dep.teamBName + ".jar");
			}

		} catch (IOException e) {
			_log.error("Could not create player or map file", e);
		}
		return needRestart;
	}

	@Override
	public synchronized void addPacket(Packet p) {
		int core;
		switch (p.getCmd()) {
		case RUN:
			// Find a free core
			for (core = 0; core < cores; core++) {
				if (running[core] == null)
					break;
			}
			// Could not find free core
			if (core == cores)
				break;

			NetworkMatch match = (NetworkMatch) p.get(0);
			DependencyHashes deps = (DependencyHashes) p.get(1);
			MatchRunner m = new MatchRunner(this, match, core);
			running[core] = m;
			if (resolveDependencies(match, deps)) {
				new Thread(m).start();
			}
			break;
		case ANALYZE:
			// Find a free core
			for (core = 0; core < cores; core++) {
				if (running[core] == null)
					break;
			}
			// Could not find free core
			if (core == cores)
				break;
			BSScrimmageSet scrim = (BSScrimmageSet) p.get(0);
			byte[] fileData = (byte[]) p.get(1);
			DependencyHashes depHashes = (DependencyHashes) p.get(2);
			MatchRunner mr = new MatchRunner(this, scrim, fileData, core);
			running[core] = mr;
			if (resolveDependencies(depHashes)) {
				new Thread(mr).start();
			}
			break;
		case DEPENDENCIES:
			Dependencies dep = (Dependencies) p.get(0);

			boolean needRestart = writeDependencies(dep);
			try {
				// If we only requested battlecode-server.jar and idata, the response won't have teams
				if (dep.teamAName != null) {
					String team_a = dep.teamAName.replaceAll("\\W", "_");
					MatchRunner.compilePlayer("A" + team_a, "teams" + File.separator + dep.teamAName + ".jar");
				}
				if (dep.teamBName != null) {
					String team_b = dep.teamBName.replaceAll("\\W", "_");
					MatchRunner.compilePlayer("B" + team_b, "teams" + File.separator + dep.teamBName + ".jar");
				}

			} catch (IOException e1) {
				_log.error("Error compiling player", e1);
				break;
			}
			if (needRestart) {
				_log.info("battlecode-server.jar updated, restarting worker");
				System.exit(Config.RESTART_STATUS);
			}
			for (MatchRunner matchRunner: running) {
				if (!matchRunner.isRunning()) {
					new Thread(matchRunner).start();
				}
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
			File[] garbageDirs = new File("teams").listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return !pathname.getAbsolutePath().endsWith(".jar");
				}
			});
			for (File f: garbageDirs) {
				try {
					FileUtils.deleteDirectory(f);
				} catch (IOException e) {
					_log.warn("Error cleaning up compiled player dir: " + f.getAbsolutePath(), e);
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

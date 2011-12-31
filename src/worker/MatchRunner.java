package worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import main.Main;
import model.BSMatch;
import model.MatchResult;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import battlecode.server.Server;
import battlecode.server.controller.Controller;
import battlecode.server.controller.ControllerFactory;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.ProxyFactory;

import common.Config;
import common.NetworkMatch;
import common.Util;

/**
 * This class handles the running of battlecode matches and returning the results
 * @author stevearc
 *
 */

public class MatchRunner implements Runnable {
	private static Logger _log = Logger.getLogger(MatchRunner.class);
	private Worker worker;
	private boolean running = true;
	private NetworkMatch match;
	private int core;
	private Process currentProcess;

	public MatchRunner(Worker worker, NetworkMatch match, int core) {
		this.match = match;
		this.worker = worker;
		this.core = core;
	}

	/**
	 * Safely halts the execution of the MatchRunner
	 */
	public void stop() {
		running = false;
		if (currentProcess != null) {
			currentProcess.destroy();
		}
	}

	private static void extractAndRenameTeam(String jarfile, String team) throws IOException {
		JarFile jar = new JarFile(jarfile);
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry file = entries.nextElement();
			File f = new File("teams/tmp/" + file.getName());
			if (file.isDirectory()) {
				f.mkdir();
				continue;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(jar.getInputStream(file)));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			String line;
			while ((line = br.readLine()) != null) {
				bw.write(line.replaceAll("package team[0-9]{3}", "package " + team).replaceAll("import team[0-9]{3}", "import " + team));
				bw.newLine();
			}
			br.close();
			bw.close();
		}
	}

	public static void compilePlayer(String teamName, String jarFile) throws IOException {
		File aPlayer = new File("teams/" + teamName);
		if (!aPlayer.exists()) {
			_log.info("Compiling player " + teamName);
			aPlayer.mkdir();
			File workingDir = new File("teams/tmp");
			workingDir.mkdir();
			extractAndRenameTeam(jarFile, teamName);
			Collection<File> srcFiles = FileUtils.listFiles(workingDir, new String[] {"java"}, true);
			String[] srcFileNames = new String[srcFiles.size()];
			int index = 0;
			for (File f: srcFiles) {
				srcFileNames[index++] = f.getAbsolutePath();
			}
			String[] javacArgs = new String[4 + srcFiles.size()];
			javacArgs[0] = "-classpath";
			javacArgs[1] = "lib/battlecode-server.jar";
			javacArgs[2] = "-d";
			javacArgs[3] = "teams/";
			System.arraycopy(srcFileNames, 0, javacArgs, 4, srcFiles.size());
			com.sun.tools.javac.Main.compile(javacArgs);
			FileUtils.deleteDirectory(workingDir);
		}
	}
	
	/**
	 * Runs the battlecode match
	 */
	public void run() {
		_log.info("Running: " + match);
		if (Config.MOCK_WORKER) {
			try {
				Thread.sleep(1000 * Config.MOCK_WORKER_SLEEP);
			} catch (InterruptedException e) {
			}
			if (running) {
				worker.matchFinish(this, core, match, BSMatch.STATUS.FINISHED, MatchResult.constructMockMatchResult(), new byte[0]);
			}
			return;
		}


		try {
			// Run the match inside this JVM if we're the first core.  Otherwise we need to start a new process
			if (core == 0) {
				runMatch(match.seed, match.map.getMapName(), match.team_a, match.team_b);
			} else {
				_log.debug("Forking process");
				Runtime run = Runtime.getRuntime();
				Process p = run.exec(new String[] {"/bin/bash", "run.sh", "-" + Main.runMatchArg, match.seed + "", match.map.getMapName(), match.team_a, match.team_b});
				p.waitFor();
			}
			
			String matchFile = match.seed + match.map.getMapName() + ".rms";
			
			if (!running) {
				new File(matchFile).delete();
				return;
			}

			// Read in the replay file
			GameData gameData = new GameData(matchFile);
			byte[] data;
			try {
				data = Util.getFileData(matchFile);
			} catch (IOException e) {
				if (running) {
					_log.error("Failed to read " + matchFile, e);
					worker.matchFailed(this, core, match);
				}
				return;
			}
			
			new File(matchFile).delete();

			if (running) {
				_log.info("Finished: " + match);
				worker.matchFinish(this, core, match, BSMatch.STATUS.FINISHED, gameData.analyzeMatch(), data);
			}
		} catch (IOException e) {
			if (running) {
				_log.error("Failed to run match", e);
				worker.matchFailed(this, core, match);
			}
			return;
		} catch (ClassNotFoundException e) {
			if (running) {
				_log.error("Failed to read match file", e);
				worker.matchFailed(this, core, match);
			}
		} catch (InterruptedException e) {
			if (running) {
				_log.error("Interrupted while running match", e);
				worker.matchFailed(this, core, match);
			}
		}
	}
	
	public static void runMatch(long seed, String mapName, String team_a, String team_b) throws IOException {
		// Construct the map file with the appropriate seeeeeed
		File seededMap = new File("maps/" + seed + mapName + ".xml");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("maps/" + mapName + ".xml"))));
		BufferedWriter fos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(seededMap)));
		String line;
		while ((line = br.readLine()) != null) {
			fos.write(line.replaceAll("seed=[^ ]*", "seed=\"" + seed + "\""));
			fos.newLine();
		}
		br.close();
		fos.close();
		
		battlecode.server.Config bcConfig = battlecode.server.Config.getGlobalConfig();
		bcConfig.set("bc.engine.debug-methods", "false");
		bcConfig.set("bc.game.maps", seed + mapName);
		bcConfig.set("bc.game.team-a", "A" + team_a);
		bcConfig.set("bc.game.team-b", "B" + team_b);
		bcConfig.set("bc.server.mode", "headless");
        bcConfig.set("bc.engine.silence-a", "true");
        bcConfig.set("bc.engine.silence-b", "true");
		Controller controller = ControllerFactory
				.createHeadlessController(bcConfig);
		Proxy[] proxies = new Proxy[] { 
				ProxyFactory.createProxyFromFile(seed + mapName + ".rms"),
		};
		Server bcServer = new Server(bcConfig, Server.Mode.HEADLESS, controller, proxies);
		controller.addObserver(bcServer);
		bcServer.run();
		
		seededMap.delete();
	}

	@Override
	public String toString() {
		return match.toString();
	}

}

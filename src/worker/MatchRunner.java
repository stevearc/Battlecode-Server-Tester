package worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import model.BSMatch;
import model.MatchResult;

import org.apache.log4j.Logger;

import battlecode.server.Server;
import battlecode.server.ServerFactory;

import common.Config;
import common.NetworkMatch;
import common.Util;

/**
 * This class handles the running of battlecode matches and returning the results
 * @author stevearc
 *
 */

public class MatchRunner implements Runnable {
	private Logger _log = Logger.getLogger(MatchRunner.class);
	private Worker worker;
	private NetworkMatch match;
	private boolean stop = false;
	private Process curProcess;
	private int core;

	public MatchRunner(Worker worker, NetworkMatch match, int core) {
		this.match = match;
		this.worker = worker;
		this.core = core;
	}

	/**
	 * Safely halts the execution of the MatchRunner
	 */
	public void stop() {
		stop = true;
		if (curProcess != null)
			curProcess.destroy();
	}

	private void printOutput() {
		if (Config.PRINT_WORKER_OUTPUT && !stop && curProcess != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(curProcess.getInputStream()));
			try {
				String line = reader.readLine();
				do {
					System.out.println(line);
				} while ((line = reader.readLine()) != null);
			} catch (IOException e) {
				_log.warn("Could not read script output", e);
			}
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
			worker.matchFinish(this, core, match, BSMatch.STATUS.FINISHED, MatchResult.constructMockMatchResult(), new byte[0]);
			return;
		}
		
		String team_a = match.team_a.replaceAll("\\W", "_");
		String team_b = match.team_b.replaceAll("\\W", "_");
		try {
			Runtime run = Runtime.getRuntime();

			if (stop)
				return;
			File coreWorkspace = new File("core" + core);
			if (!coreWorkspace.exists()) {
				coreWorkspace.mkdir();
			}
			
			// Rename team A in the source
			curProcess = run.exec(new String[] {Config.cmd_rename_team, 
					"./core" + core, 
					"A" + team_a, 
					"./teams/" + match.team_a + ".jar"});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;
			
			// Rename team B in the source
			curProcess = run.exec(new String[] {Config.cmd_rename_team, 
					"./core" + core, 
					"B" + team_b, 
					"./teams/" + match.team_b + ".jar"});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;
			
			// Construct the map file with the appropriate seeeeeed
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("maps/" + match.map.getMapName() + ".xml"))));
			BufferedWriter fos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("maps/" + match.seed + match.map.getMapName() + ".xml"))));
			String line;
			while ((line = br.readLine()) != null) {
				fos.write(line.replaceAll("seed=[^ ]*", "seed=\"" + match.seed + "\""));
				fos.newLine();
			}
			br.close();
			fos.close();
			
			battlecode.server.Config bcConfig = battlecode.server.Config.getGlobalConfig();
			bcConfig.set("bc.engine.debug-methods", "false");
			bcConfig.set("bc.game.maps", match.seed + match.map.getMapName());
			bcConfig.set("bc.game.team-a", "A" + match.team_a);
			bcConfig.set("bc.game.team-b", "B" + match.team_b);
			bcConfig.set("bc.server.save-file", match.map + ".rms");
            bcConfig.set("bc.server.mode", "headless");
            bcConfig.set("bc.server.debug", "true");
            Server bcServer = ServerFactory.createHeadlessServer(bcConfig, "core" + core + "/" + match.map + ".rms");
            bcServer.run();
            
            new File("maps/" + match.seed + match.map.getMapName() + ".xml").delete();
            
		} catch (Exception e) {
			if (!stop) {
				_log.error("Failed to run match", e);
				worker.matchFailed(this, core, match);
			}
			return;
		}

		// Read in the replay file
		String matchFile = "./core" + core + "/" + match.map + ".rms";
		byte[] data;
		GameData gameData;
		try {
			data = Util.getFileData(matchFile);
			gameData = new GameData(matchFile);
		} catch (IOException e) {
			if (!stop) {
				_log.error("Failed to read " + matchFile, e);
				worker.matchFailed(this, core, match);
			}
			return;
		} catch (ClassNotFoundException e) {
			if (!stop) {
				_log.error("Failed to read " + matchFile, e);
				worker.matchFailed(this, core, match);
			}
			return;
		}

		if (!stop) {
			_log.info("Finished: " + match);
			worker.matchFinish(this, core, match, BSMatch.STATUS.FINISHED, gameData.analyzeMatch(), data);
		}
	}
	
	@Override
	public String toString() {
		return match.toString();
	}

}

class Callback implements Runnable {
	private Thread parent;
	private Process proc;

	public Callback (Thread parent, Process proc) {
		this.parent = parent;
		this.proc = proc;
	}

	@Override
	public void run() {
		try {
			proc.waitFor();
			parent.interrupt();
		} catch (InterruptedException e) {
		}
	}

}
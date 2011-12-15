package worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.BSMatch;

import common.Config;
import common.NetworkMatch;
import common.Util;

/**
 * This class handles the running of battlecode matches and returning the results
 * @author stevearc
 *
 */

public class MatchRunner implements Runnable {
	private static final boolean PRINT_OUTPUT = true;
	private Logger _log;
	private Config config;
	private Worker worker;
	private NetworkMatch match;
	private boolean stop = false;
	private Process curProcess;
	private int core;

	public MatchRunner(Worker worker, NetworkMatch match, int core) {
		config = Config.getConfig();
		this.match = match;
		this.worker = worker;
		this.core = core;
		_log = config.getLogger();
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
		if (PRINT_OUTPUT && !stop && curProcess != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(curProcess.getInputStream()));
			try {
				String line = reader.readLine();
				do {
					System.out.println(line);
				} while ((line = reader.readLine()) != null);
			} catch (IOException e) {
				_log.log(Level.WARNING, "Could not read script output", e);
			}
		}
	}
	
	/**
	 * Runs the battlecode match
	 */
	public void run() {
		String team_a = match.team_a.replaceAll("\\W", "_");
		String team_b = match.team_b.replaceAll("\\W", "_");
		try {
			_log.info("Running: " + match);
			String output_file = config.install_dir + "/battlecode/core" + core + "/" + match.map + match.seed + ".out";
			Runtime run = Runtime.getRuntime();

			if (stop)
				return;
			// Generate the bc.conf file
			curProcess = run.exec(new String[] {config.cmd_gen_conf, 
					config.install_dir + "/battlecode/core" + core, 
					match.map.getMapName(),
					"A" + team_a,
					"B" + team_b,
					""+match.seed});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;
			
			// Rename team A in the source
			curProcess = run.exec(new String[] {config.cmd_rename_team, 
					config.install_dir + "/battlecode/core" + core, 
					"A" + team_a, 
					config.install_dir + "/battlecode/teams/" + match.team_a + ".jar"});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;
			
			// Rename team B in the source
			curProcess = run.exec(new String[] {config.cmd_rename_team, 
					config.install_dir + "/battlecode/core" + core, 
					"B" + team_b, 
					config.install_dir + "/battlecode/teams/" + match.team_b + ".jar"});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;
			
			// Run the match
			curProcess = run.exec(new String[] {config.cmd_run_match, config.install_dir + "/battlecode/core" + core, output_file});
			Thread.sleep(10000);
			new Thread(new Callback(Thread.currentThread(), curProcess)).start();
			try {
				Thread.sleep(config.timeout);
			} catch (InterruptedException e) {
			}

			// Read in the output file
			BufferedReader reader = new BufferedReader(new FileReader(output_file));
			StringBuilder sb = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null; ) {
				sb.append(line);
			}
			String output = sb.toString();
			File of = new File(output_file);
			of.delete();

			// Check to see if there were any errors
			if (stop)
				return;
			if (curProcess.exitValue() != 0) {
				_log.severe("Error running match\n" + output);
				worker.matchFailed(core, match);
				return;
			}

			_log.info("Finished: " + match);
		} catch (Exception e) {
			if (!stop) {
				_log.log(Level.SEVERE, "Failed to run match", e);
				worker.matchFailed(core, match);
			}
			return;
		}

		// Read in the replay file
		String matchFile = config.install_dir + "/battlecode/core" + core + "/" + match.map + ".rms";
		byte[] data;
		GameData gameData;
		try {
			data = Util.getFileData(matchFile);
			gameData = new GameData(matchFile);
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Failed to read " + matchFile, e);
			if (!stop)
				worker.matchFailed(core, match);
			return;
		} catch (ClassNotFoundException e) {
			_log.log(Level.SEVERE, "Failed to read " + matchFile, e);
			if (!stop)
				worker.matchFailed(core, match);
			return;
		}

		if (!stop) {
			worker.matchFinish(core, match, BSMatch.STATUS.FINISHED, gameData.analyzeMatch(), data);
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
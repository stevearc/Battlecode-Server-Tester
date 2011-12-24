package worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import model.BSMatch;
import model.MatchResult;

import org.apache.log4j.Logger;

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
			String output_file = "./battlecode/core" + core + "/" + match.map + match.seed + ".out";
			Runtime run = Runtime.getRuntime();

			if (stop)
				return;
			// Generate the bc.conf file
			curProcess = run.exec(new String[] {Config.cmd_gen_conf, 
					"./battlecode/core" + core, 
					match.map.getMapName(),
					"A" + team_a,
					"B" + team_b,
					""+match.seed});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;
			
			// Rename team A in the source
			curProcess = run.exec(new String[] {Config.cmd_rename_team, 
					"./battlecode/core" + core, 
					"A" + team_a, 
					"./battlecode/teams/" + match.team_a + ".jar"});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;
			
			// Rename team B in the source
			curProcess = run.exec(new String[] {Config.cmd_rename_team, 
					"./battlecode/core" + core, 
					"B" + team_b, 
					"./battlecode/teams/" + match.team_b + ".jar"});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;
			
			// Run the match
			curProcess = run.exec(new String[] {Config.cmd_run_match, "./battlecode/core" + core, output_file});
			curProcess.waitFor();
			printOutput();
			if (stop)
				return;

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
				_log.error("Error running match\n" + output);
				worker.matchFailed(this, core, match);
				return;
			}

		} catch (Exception e) {
			if (!stop) {
				_log.error("Failed to run match", e);
				worker.matchFailed(this, core, match);
			}
			return;
		}

		// Read in the replay file
		String matchFile = "./battlecode/core" + core + "/" + match.map + ".rms";
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
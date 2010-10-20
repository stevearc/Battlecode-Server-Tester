package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Config;
import common.Match;

/**
 * This class handles the running of battlecode matches and returning the results
 * @author steven
 *
 */

public class MatchRunner implements Runnable {
	private Logger _log;
	private Config config;
	private Client client;
	private Match match;
	private boolean stop = false;
	private ReentrantLock repoLock;
	private Process curProcess;

	public MatchRunner(Client client, Match match, ReentrantLock repoLock) {
		config = Config.getConfig();
		this.match = match;
		this.client = client;
		this.repoLock = repoLock;
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

	/**
	 * Runs the battlecode match
	 */
	public void run() {
		String status = null;
		int winner = 0;
		byte[] data = null;
		// Clean out old data
		File f = new File(config.repo + "/" + match.map + ".rms");
		f.delete();

		try {
			_log.info("Running: " + match.map);
			Runtime run = Runtime.getRuntime();
			try {
				repoLock.lock();

				// Update the repository
				if (stop)
					return;
				curProcess = run.exec(config.cmd_update);
				curProcess.waitFor();
				if (stop)
					return;
				// Get the appropriate teams for the match
				curProcess = run.exec(new String[] {config.cmd_grabole, match.team_a, match.team_b});
				curProcess.waitFor();
				if (stop)
					return;
				// Generate the bc.conf file
				curProcess = run.exec(new String[] {config.cmd_gen_conf, match.map});
				curProcess.waitFor();
				if (stop)
					return;
				// Run the match
				_log.info("Starting ant - " + match.map);
				curProcess = run.exec(new String[] {"ant", "file", "-f", config.repo + "/build.xml"});
				Thread.sleep(10000);
			} finally {
				repoLock.unlock();
			}
//			curProcess.waitFor();
			new Thread(new Callback(Thread.currentThread(), curProcess)).start();
			try {
			Thread.sleep(config.timeout);
			} catch (InterruptedException e) {
			}
			_log.info("Finished ant - " + match.map);

			Writer writer = new StringWriter();
			BufferedReader read = new BufferedReader(new InputStreamReader(curProcess.getInputStream()));
			char[] buffer = new char[1024];
			int n;
			while ((n = read.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
			String output = writer.toString();

			if (stop)
				return;
			if (curProcess.exitValue() != 0) {
				_log.severe("Error running match\n" + output);
				status = "error";
				client.matchFinish(this, match, status, winner, data);
				return;
			}

			// Kind of sloppy win detection, but it works
			int a_index = output.indexOf("(A) wins");
			int b_index = output.indexOf("(B) wins");
			if (a_index == -1) {
				if (b_index == -1) {
					status = "error";
					_log.warning("Unknown error running match\n" + output);
					_log.warning("^ error: a_index: " + a_index + " b_index: " + b_index);
					if (!stop) 
						client.matchFinish(this, match, status, winner, data);
					return;
				}
				winner = 0;
			} else {
				winner = 1;
			}

			_log.info("Finished: " + match.map);
		} catch (Exception e) {
			if (!stop) {
				_log.log(Level.SEVERE, "Failed to run match", e);
				status = "error";
				client.matchFinish(this, match, status, winner, data);
			}
			return;
		}

		String matchFile = config.repo + "/" + match.map + ".rms";
		try {
			data = getMatchData(matchFile);
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Failed to read " + matchFile, e);
			status = "error";
			if (!stop)
				client.matchFinish(this, match, status, winner, data);
			return;
		}

		status = "ok";
		if (!stop)
			client.matchFinish(this, match, status, winner, data);
	}

	/**
	 * 
	 * @param filename
	 * @return The data from the match.rms file
	 * @throws IOException
	 */
	private byte[] getMatchData(String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists())
			throw new IOException("Match file does not exist");
		FileInputStream is = new FileInputStream(file);

		long length = file.length();

		// Create the byte array to hold the data
		byte[] data = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < data.length
				&& (numRead=is.read(data, offset, data.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < data.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}

		is.close();
		return data;
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
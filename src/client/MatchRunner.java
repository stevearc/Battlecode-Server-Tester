package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import networking.Packet;
import networking.PacketType;

import common.Config;

/**
 * This class handles the running of battlecode matches and returning the results
 * @author steven
 *
 */

public class MatchRunner implements Runnable {
	private Logger _log;
	private Config config;
	private Packet packet;
	private Packet response = null;
	private boolean stop = false;
	private ReentrantLock repoLock;
	private Runner runner = null;

	public MatchRunner(Config config, Packet packet, ReentrantLock repoLock) {
		this.config = config;
		this.packet = packet;
		this.repoLock = repoLock;
		_log = config.getClientLogger();
	}

	/**
	 * Safely halts the execution of the MatchRunner
	 */
	public void stop() {
		stop = true;
		runner.terminate();
	}

	/**
	 * Runs the battlecode match
	 */
	public void run() {
		Packet responsePacket = new Packet(PacketType.MAP_RESULT, packet.map, packet.team_a, packet.team_b, 0, false, null);
		byte[] matchData;
		// Clean out the old data
		File oldMatchFile = new File(config.repo + "/match.rms");
		oldMatchFile.delete();

		try {
			_log.info("Running: " + packet.map);
			Runtime run = Runtime.getRuntime();
			try {
				repoLock.lock();

				// Update the repository
				Process p = Runtime.getRuntime().exec(config.cmd_update);
				p.waitFor();
				// Get the appropriate teams for the match
				p = run.exec(new String[] {config.cmd_grabole, packet.team_a, packet.team_b});
				p.waitFor();
				// Generate the bc.conf file
				p = run.exec(new String[] {config.cmd_gen_conf, packet.map});
				p.waitFor();


				/*
				 * We create the thread that will run the match, then sleep.
				 * When the thread running the match finishes, it will interrupt the current thread, which
				 * was sleeping.  We know that if we were interrupted, the match finished correctly.  
				 * Otherwise, if we sleep for the full amount of time, we know something probably went wrong.
				 */
				runner = new Runner(Thread.currentThread(), new String[] {"ant", "file", "-f", config.repo + "/build.xml"});
				new Thread(runner).start();
				Thread.sleep(1000);
			} finally {
				repoLock.unlock();
			}
			try {
				Thread.sleep(config.timeout * 1000);
				runner.terminate();
				// If we stopped it manually, there was no problem.  Otherwise, log it.
				if (!stop) 
					_log.severe("Timed out while running match");

				response = responsePacket;
				return;
			} catch (InterruptedException e) {
				// The runner thread should interrupt us when it finishes, 
				// taking us here instead of to the runner.terminate() line above

			}

			String output = runner.getOutput();
			if (output == null) {
				// Keyboard Interrupt
				response = responsePacket;
				return;
			}

			// Kind of sloppy win detection, but it works
			if (output.indexOf("(A) wins") != -1) {
				responsePacket.a_wins = true;
			}

			_log.info("Finished: " + packet.map);
		} catch (Exception e) {
			_log.severe("Failed to run match");
			if (config.DEBUG)
				e.printStackTrace();
			response = responsePacket;
			return;
		}
		
		String matchFile = config.repo + "/" + packet.map + ".rms";
		try {
			matchData = getMatchData(matchFile);
		} catch (IOException e) {
			_log.severe("Failed to read " + matchFile);
			if (config.DEBUG)
				e.printStackTrace();
			response = responsePacket;
			return;
		}

		responsePacket.match = matchData;
		responsePacket.matchLength = responsePacket.match.length;
		response = responsePacket;
	}

	/**
	 * Fetch the result when the match is finished. 
	 * @return Returns null if the match is still running.  Returns the packet to send to the server if finished.
	 */
	public Packet getResponse() {
		return response;
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

}

class Runner implements Runnable {
	private String[] commands;
	private Thread parent;
	private Process p;
	private String output;

	public Runner(Thread parent, String[] commands){
		this.commands = commands;
		this.parent = parent;
	}

	@Override
	public void run() {
		try {
			p = Runtime.getRuntime().exec(commands);
			p.waitFor();
			if (p.exitValue() == 0) {
				BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()));
				StringBuffer sb = new StringBuffer();
				for (String line = read.readLine(); line != null; line = read.readLine()) {
					sb.append(line);
				}
				output = sb.toString();
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
			p.destroy();
		} finally {
			parent.interrupt();
		}
	}

	public void terminate() {
		p.destroy();
	}

	/**
	 * 
	 * @return The stdout of the process, or null if it is not available
	 */
	public String getOutput() {
		try {
			if (p.exitValue() == 0)
				return output;
		} catch (IllegalThreadStateException e) {
		} 
		return null;
	}

}

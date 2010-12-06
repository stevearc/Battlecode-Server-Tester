package backend;

import java.sql.SQLException;

import networking.Packet;

import common.Config;

/**
 * Provides static methods to perform asynchronous calls to the Server
 * @author stevearc
 *
 */
public class ServerMethodCaller {

	/**
	 * Queues a run
	 * @param run
	 * @param seeds
	 * @param maps
	 */
	public static void queueRun(final String team_a, final String team_b, final String[] seeds, final String[] maps) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().queueRun(team_a, team_b, seeds, maps);
			}

		}).start();
	}

	/**
	 * Delete run data
	 * @param run_id
	 */
	public static void deleteRun(final int run_id) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().deleteRun(run_id);
			}

		}).start();
	}

	/**
	 * Start the server if it hasn't been running
	 */
	public static void startServer() {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().start();
			}

		}).start();
	}

	/**
	 * Send the finished match data to the server
	 * @param client
	 * @param p
	 */
	public static void matchFinished(final ClientRepr client, final Packet p) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().matchFinished(client, p);
			}

		}).start();
	}

	/**
	 * Tell the server to send matches to a client
	 * @param client
	 */
	public static void sendClientMatches(final ClientRepr client) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getServer().sendClientMatches(client);
			}

		}).start();
	}
	
	/**
	 * Update the repository
	 */
	public static void updateRepo() {
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					Config.getServer().updateRepo();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}).start();
	}

}

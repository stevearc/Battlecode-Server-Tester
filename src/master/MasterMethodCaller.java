package master;

import networking.Packet;

import common.Config;

/**
 * Provides static methods to perform asynchronous calls to the master
 * @author stevearc
 *
 */
public class MasterMethodCaller {

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
				Config.getMaster().queueRun(team_a, team_b, seeds, maps);
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
				Config.getMaster().deleteRun(run_id);
			}

		}).start();
	}

	/**
	 * Start the master if it hasn't been running
	 */
	public static void startMaster() {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getMaster().start();
			}

		}).start();
	}

	/**
	 * Send the finished match data to the master
	 * @param worker
	 * @param p
	 */
	public static void matchFinished(final WorkerRepr worker, final Packet p) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getMaster().matchFinished(worker, p);
			}

		}).start();
	}

	/**
	 * Tell the master to send matches to a worker
	 * @param worker
	 */
	public static void sendWorkerMatches(final WorkerRepr worker) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getMaster().sendWorkerMatches(worker);
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
				Config.getMaster().updateMaps();
			}

		}).start();
	}

}

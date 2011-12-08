package master;

import java.io.File;
import java.util.List;

import networking.Packet;

import common.Config;
import common.NetworkMatch;

/**
 * Provides static methods to perform asynchronous calls to the master
 * @author stevearc
 *
 */
public class MasterMethodCaller {

	/**
	 * Update the battlecode source files
	 * @param battlecode_server
	 * @param idata
	 */
	public static void updateBattlecodeFiles(final File battlecode_server, final File idata) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getMaster().updateBattlecodeFiles(battlecode_server, idata);
			}

		}).start();
	}
	/**
	 * Queues a run
	 * @param run
	 * @param seeds
	 * @param maps
	 */
	public static void queueRun(final Long teamAId, final Long teamBId, final List<Long> seeds, final List<Long> mapIds) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getMaster().queueRun(teamAId, teamBId, seeds, mapIds);
			}

		}).start();
	}
	
	/**
	 * Delete run data
	 * @param run_id
	 */
	public static void deleteRun(final Long run_id) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				Config.getMaster().deleteRun(run_id);
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
	 * Tell the master to send player/map data to the worker
	 * @param worker
	 * @param match
	 * @param needMap
	 * @param needTeamA
	 * @param needTeamB
	 */
	public static void sendWorkerMatchDependencies(final WorkerRepr worker, final NetworkMatch match, final boolean needUpdate, 
			final boolean needMap, final boolean needTeamA, final boolean needTeamB) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Config.getMaster().sendWorkerMatchDependencies(worker, match, needUpdate, needMap, needTeamA, needTeamB);
			}
			
		}).start();
	}
}

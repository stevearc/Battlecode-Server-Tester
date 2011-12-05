package master;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import networking.CometCmd;
import networking.CometMessage;
import networking.Packet;
import beans.BSMap;
import beans.BSMatch;
import beans.BSPlayer;
import beans.BSRun;

import common.Config;
import common.Dependencies;
import common.NetworkMatch;
import common.Util;

import db.HibernateUtil;

/**
 * Handles distribution of load to connected workers
 * @author stevearc
 *
 */
public class Master {
	private Config config;
	private Logger _log;
	private NetworkHandler handler;
	private HashSet<WorkerRepr> workers = new HashSet<WorkerRepr>();
	private HashSet<BSMap> maps = new HashSet<BSMap>();
	private WebPollHandler wph;

	public Master() throws Exception {
		config = Config.getConfig();
		wph = Config.getWebPollHandler();
		_log = config.getLogger();
		handler = new NetworkHandler();
	}

	/**
	 * Start running the master
	 */
	public synchronized void start() {
		new Thread(handler).start();
		startRun();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					updateMaps();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	/**
	 * Add a run to the queue
	 * @param run
	 * @param seeds
	 * @param mapNames
	 */
	public synchronized void queueRun(String teamA, String teamB, String[] seeds, String[] maps) {
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun newRun = new BSRun();
		newRun.setTeamA(teamA);
		newRun.setTeamB(teamB);
		em.persist(newRun);
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
		em.refresh(newRun);

		for (String seed: seeds) {
			Long seedLong = new Long(Integer.parseInt(seed));
			for (BSMap map: maps) {
				BSMatch match = new BSMatch();
				match.setMap(map);
				match.setSeed(seedLong);
				match.setRun(newRun);
				em.persist(match);
			}
		}
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
		em.close();
		wph.broadcastMsg("matches", new CometMessage(CometCmd.INSERT_TABLE_ROW, new String[] {""+newRun.getId(), 
				teamA.getPlayerName(), teamB.getPlayerName()}));
		startRun();
	}

	/**
	 * Delete run data
	 * @param runId
	 */
	public synchronized void deleteRun(int runId) {
		BSRun currentRun = getCurrentRun();
		if (currentRun == null)
			return;
		// If it's running right now, just cancel it
		if (currentRun.getStatus() == BSRun.STATUS.RUNNING) {
			stopCurrentRun(BSRun.STATUS.CANCELED);
			startRun();
		} else {
			// otherwise, delete it
			EntityManager em = HibernateUtil.getEntityManager();
			em.remove(currentRun);
			em.close();
			wph.broadcastMsg("matches", new CometMessage(CometCmd.DELETE_TABLE_ROW, new String[] {""+runId}));
		}
	}

	/**
	 * Process a worker connecting
	 * @param worker
	 */
	public synchronized void workerConnect(WorkerRepr worker) {
		_log.info("Worker connected: " + worker);
		workers.add(worker);
		wph.broadcastMsg("connections", new CometMessage(CometCmd.INSERT_TABLE_ROW, new String[] {worker.toHTML()}));
		sendWorkerMatches(worker);
	}

	/**
	 * Process a worker disconnecting
	 * @param worker
	 */
	public synchronized void workerDisconnect(WorkerRepr worker) {
		_log.info("Worker disconnected: " + worker);
		wph.broadcastMsg("connections", new CometMessage(CometCmd.DELETE_TABLE_ROW, new String[] {worker.toHTML()}));
		workers.remove(worker);
	}

	/**
	 * Save data from a finished match
	 * @param worker
	 * @param p
	 */
	public synchronized void matchFinished(WorkerRepr worker, Packet p) {
		/* TODO
		NetworkMatch m = (NetworkMatch) p.get(0);
		String status = (String) p.get(1);
		int winner = (Integer) p.get(2);
		int win_condition = (Integer) p.get(3);
		double a_points = (Double) p.get(4);
		double b_points = (Double) p.get(5);
		byte[] data = (byte[]) p.get(6);
		wph.broadcastMsg("connections", new CometMessage(CometCmd.REMOVE_MAP, new String[] {worker.toHTML(), m.toMapString()}));
		try {
			if ("ok".equals(status) && m.run_id == getCurrentId()) {
				if (!getMatchesLeft().contains(m)) {
					_log.info("Received duplicate match: " + m);
					sendWorkerMatches(worker);
					return;
				}
				_log.info("Match finished: " + m + " winner: " + (winner == 1 ? "A" : "B"));
				FileOutputStream fos = new FileOutputStream("battlecode/matches/" + getCurrentId() + m.map.mapName + m.seed + ".rms");
				fos.write(data);
				PreparedStatement stmt = null;
				stmt = db.prepare("UPDATE matches SET win = ?, win_condition = ?, a_points = ?, b_points = ? WHERE id = ?");
				stmt.setInt(1, winner);
				stmt.setInt(2, win_condition);
				stmt.setDouble(3, a_points);
				stmt.setDouble(4, b_points);
				stmt.setInt(5, m.id);
				db.update(stmt, true);
				wph.broadcastMsg("matches", new CometMessage(CometCmd.MATCH_FINISHED, new String[] {""+m.run_id, ""+winner}));

				// If finished, start next run
				if (getMatchesLeft().isEmpty()) {
					stopCurrentRun(Config.STATUS_COMPLETE);
					startRun();
				}

			} else {
				_log.warning("Match " + m + " on worker " + worker + " failed");
			}

			if (getMatchesLeft().isEmpty()) {
				stopCurrentRun(Config.STATUS_COMPLETE);
				startRun();
			} else {
				sendWorkerMatches(worker);
			}
		} catch (SQLException e) {
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Error writing match file", e);
		}
		*/
	}

	/**
	 * Send matches to a worker until they are saturated
	 * @param worker
	 */
	public synchronized void sendWorkerMatches(WorkerRepr worker) {
		List<BSMatch> queuedMatches = getMatchesWithStatus(BSMatch.STATUS.QUEUED);
		EntityManager em = HibernateUtil.getEntityManager();

		for (int i = 0; i < queuedMatches.size() && worker.isFree(); i++) {
			BSMatch m = queuedMatches.get(i);
			NetworkMatch nm = m.buildNetworkMatch();
			_log.info("Sending match " + nm + " to worker " + worker);
			wph.broadcastMsg("connections", new CometMessage(CometCmd.ADD_MAP, new String[] {worker.toHTML(), nm.toMapString()}));
			worker.runMatch(nm);
			m.setStatus(BSMatch.STATUS.RUNNING);
			em.merge(m);
		}
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
		em.close();

		// If we are currently running all necessary maps, add some redundancy by
		// Sending this worker random maps that other workers are currently running
		if (worker.isFree()) {
			Random r = new Random();
			List<BSMatch> runningMatches = getMatchesWithStatus(BSMatch.STATUS.RUNNING);
			while (!runningMatches.isEmpty() && worker.isFree()) {
				NetworkMatch nm = runningMatches.get(r.nextInt()%runningMatches.size()).buildNetworkMatch();
				_log.info("Sending match " + nm + " to worker " + worker);
				wph.broadcastMsg("connections", new CometMessage(CometCmd.ADD_MAP, new String[] {worker.toHTML(), nm.toMapString()}));
				worker.runMatch(nm);
			}
		}
	}

	public synchronized void sendWorkerMatchDependencies(WorkerRepr worker, NetworkMatch match, boolean needMap, boolean needTeamA, boolean needTeamB) {
		Dependencies dep;
		byte[] map = null;
		byte[] teamA = null;
		byte[] teamB = null;
		try {
			if (needMap) {
				map = Util.getFileData(config.install_dir + "/battlecode/maps/" + match.map.mapName + ".xml");
			}
			if (needTeamA) {
				teamA = Util.getFileData(config.install_dir + "/battlecode/teams/" + match.team_a + ".jar");
			}
			if (needTeamB) {
				teamB = Util.getFileData(config.install_dir + "/battlecode/teams/" + match.team_b + ".jar");
			}
			dep = new Dependencies(match.map.mapName, map, match.team_a, teamA, match.team_b, teamB);
			_log.info("Sending " + worker + " " + dep);
			worker.runMatchWithDependencies(match, dep);
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Could not read data file", e);
			wph.broadcastMsg("connections", new CometMessage(CometCmd.REMOVE_MAP, new String[] {worker.toHTML(), match.toMapString()}));
			worker.stopAllMatches();
			MasterMethodCaller.sendWorkerMatches(worker);
		}
	}

	private void startRun() {
		if (getCurrentRun() != null) {
			// Already running
			return;
		}

		EntityManager em = HibernateUtil.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<BSRun> criteria = builder.createQuery( BSRun.class );
		Root<BSRun> run = criteria.from( BSRun.class );
		criteria.select(run);
		criteria.where(builder.equal(run.get("status"), BSRun.STATUS.QUEUED));
		BSRun nextRun = null;
		try {
			nextRun = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			// pass
		}
		if (nextRun != null) {
			nextRun.setStatus(BSRun.STATUS.RUNNING);
			nextRun.setStarted(new Date());
			em.merge(nextRun);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			wph.broadcastMsg("matches", new CometMessage(CometCmd.START_RUN, 
					new String[] {""+nextRun.getId(), ""+nextRun.getMatches().size()}));
			for (WorkerRepr c: workers) {
				sendWorkerMatches(c);
			}
		}
		em.close();
	}

	private BSRun getCurrentRun() {
		EntityManager em = HibernateUtil.getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<BSRun> criteria = builder.createQuery( BSRun.class );
		Root<BSRun> run = criteria.from( BSRun.class );
		criteria.select(run);
		criteria.where(builder.equal(run.get("status"), BSRun.STATUS.RUNNING));
		BSRun currentRun = null;
		try {
			currentRun = em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			// pass
		} finally {
			em.close();
		}
		return currentRun;
	}

	private void stopCurrentRun(BSRun.STATUS status) {
		_log.info("Stopping current run");
		BSRun currentRun = getCurrentRun();
		EntityManager em = HibernateUtil.getEntityManager();
		currentRun.setStatus(status);
		currentRun.setEnded(new Date());
		em.merge(currentRun);
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
		// TODO: set status of child matches
		em.close();
		wph.broadcastMsg("matches", new CometMessage(CometCmd.FINISH_RUN, new String[] {""+currentRun.getId(), ""+status}));
		wph.broadcastMsg("connections", new CometMessage(CometCmd.FINISH_RUN, new String[] {}));
		for (WorkerRepr c: workers) {
			c.stopAllMatches();
		}
	}

	private List<BSMatch> getMatchesWithStatus(BSMatch.STATUS status) {
		BSRun currentRun = getCurrentRun();
		if (currentRun == null) {
			return new ArrayList<BSMatch>();
		}

		// TODO: maybe move this into a query?
		ArrayList<BSMatch> matches = new ArrayList<BSMatch>();
		for (BSMatch m: currentRun.getMatches()) {
			if (m.getStatus() == status) {
				matches.add(m);
			}
		}
		return matches;
	}

	/**
	 * Update the list of available maps
	 */
	public synchronized void updateMaps() {
		File file = new File("battlecode/maps");
		File[] mapFiles = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		HashSet<BSMap> newMaps = new HashSet<BSMap>();
		for (File m: mapFiles) {
			try {
				newMaps.add(new BSMap(m));
			} catch (Exception e) {
				_log.log(Level.WARNING, "Error parsing map", e);
			}
		}
		
		if (newMaps.equals(maps)) {
			return;
		}
		maps = newMaps;
		
		EntityManager em = HibernateUtil.getEntityManager();
		for (BSMap map: maps) {
			em.persist(map);
		}
		em.getTransaction().begin();
		try {
			em.flush();
		} catch (PersistenceException e) {
			// Those maps already exist; don't worry about it.
		}
		if (em.getTransaction().getRollbackOnly()) {
			em.getTransaction().rollback();
		} else {
			em.getTransaction().commit();
		}
		em.close();
	}

	/**
	 * 
	 * @return Currently connected workers
	 */
	public HashSet<WorkerRepr> getConnections() {
		return workers;
	}

	/**
	 * 
	 * @return All known maps
	 */
	@SuppressWarnings("unchecked")
	public HashSet<BSMap> getMaps() {
		return (HashSet<BSMap>) maps.clone();
	}

}
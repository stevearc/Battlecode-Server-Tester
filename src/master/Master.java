package master;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import model.BSMap;
import model.BSMatch;
import model.BSPlayer;
import model.BSRun;
import networking.CometCmd;
import networking.CometMessage;
import networking.Packet;

import common.Config;
import common.Dependencies;
import common.NetworkMatch;
import common.Util;

import dataAccess.HibernateUtil;

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
	public synchronized void queueRun(Long teamAId, Long teamBId, List<Long> seeds, List<Long> mapIds) {
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun newRun = new BSRun();
		BSPlayer teamA = em.find(BSPlayer.class, teamAId);
		BSPlayer teamB = em.find(BSPlayer.class, teamBId);
		newRun.setTeamA(teamA);
		newRun.setTeamB(teamB);
		newRun.setStatus(BSRun.STATUS.QUEUED);
		newRun.setStarted(new Date());
		em.persist(newRun);

		for (Long mapId: mapIds) {
			BSMap map = em.find(BSMap.class, mapId);
			for (Long seed: seeds) {
				BSMatch match = new BSMatch();
				match.setMap(map);
				match.setSeed(seed);
				match.setRun(newRun);
				match.setStatus(BSMatch.STATUS.QUEUED);
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
	public synchronized void deleteRun(Long runId) {
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun run = em.find(BSRun.class, runId);
		// If it's running right now, just cancel it
		if (run.getStatus() == BSRun.STATUS.RUNNING) {
			_log.info("canceling run");
			stopCurrentRun(BSRun.STATUS.CANCELED);
			startRun();
		} else {
			// TODO: delete rms files
			// otherwise, delete it
			em.getTransaction().begin();
			em.createQuery("delete from BSMatch match where match.run = ?").setParameter(1, run).executeUpdate();
			em.flush();
			em.getTransaction().commit();
			
			em.getTransaction().begin();
			em.remove(run);
			em.flush();
			em.getTransaction().commit();
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
		NetworkMatch m = (NetworkMatch) p.get(0);
		String status = (String) p.get(1);
		int winner = (Integer) p.get(2);
		int win_condition = (Integer) p.get(3);
		double a_points = (Double) p.get(4);
		double b_points = (Double) p.get(5);
		byte[] data = (byte[]) p.get(6);
		wph.broadcastMsg("connections", new CometMessage(CometCmd.REMOVE_MAP, new String[] {worker.toHTML(), m.toMapString()}));
		try {
			EntityManager em = HibernateUtil.getEntityManager();
			BSMatch match = em.find(BSMatch.class, m.id);
			if (match.getStatus() != BSMatch.STATUS.RUNNING || match.getRun().getStatus() != BSRun.STATUS.RUNNING) {
				// Match was already finished by another worker
			} else if ("ok".equals(status)) {
				match.setWinner(winner == 1 ? BSMatch.TEAM.TEAM_A : BSMatch.TEAM.TEAM_B);
				match.setWinCondition(BSMatch.WIN_CONDITION.values()[win_condition]);
				match.setaPoints(a_points);
				match.setbPoints(b_points);
				match.setStatus(BSMatch.STATUS.FINISHED);
				_log.info("Match finished: " + m + " winner: " + (winner == 1 ? "A" : "B"));
				File outFile = new File(config.install_dir + "/matches/" + match.getRun().getId() + match.getMap().getMapName() + m.seed + ".rms");
				outFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(outFile);
				fos.write(data);
				em.getTransaction().begin();
				em.merge(match);
				em.flush();
				em.getTransaction().commit();
				wph.broadcastMsg("matches", new CometMessage(CometCmd.MATCH_FINISHED, new String[] {""+m.run_id, ""+winner}));
			} else {
				_log.warning("Match " + m + " on worker " + worker + " failed");
			}

			Long matchesLeft = em.createQuery("select count(*) from BSMatch match where match.run = ? and match.status != ?", Long.class)
			.setParameter(1, match.getRun())
			.setParameter(2, BSMatch.STATUS.FINISHED)
			.getSingleResult();
			// If finished, start next run
			if (matchesLeft == 0) {
				stopCurrentRun(BSRun.STATUS.COMPLETE);
				startRun();
			} else {
				sendWorkerMatches(worker);
			}
			em.close();
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Error writing match file", e);
		}
	}

	/**
	 * Send matches to a worker until they are saturated
	 * @param worker
	 */
	public synchronized void sendWorkerMatches(WorkerRepr worker) {
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun currentRun = getCurrentRun();
		List<BSMatch> queuedMatches = em.createQuery("from BSMatch match inner join fetch match.map where match.run = ? and match.status = ?", BSMatch.class)
		.setParameter(1, currentRun)
		.setParameter(2, BSMatch.STATUS.QUEUED)
		.getResultList();

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

		// If we are currently running all necessary maps, add some redundancy by
		// Sending this worker random maps that other workers are currently running
		if (worker.isFree()) {
			Random r = new Random();
			List<BSMatch> runningMatches = em.createQuery("from BSMatch match inner join fetch match.map where match.run = ? and match.status = ?", BSMatch.class)
			.setParameter(1, currentRun)
			.setParameter(2, BSMatch.STATUS.RUNNING)
			.getResultList();
			while (!runningMatches.isEmpty() && worker.isFree()) {
				NetworkMatch nm = runningMatches.get(r.nextInt(runningMatches.size())).buildNetworkMatch();
				_log.info("Sending match " + nm + " to worker " + worker);
				wph.broadcastMsg("connections", new CometMessage(CometCmd.ADD_MAP, new String[] {worker.toHTML(), nm.toMapString()}));
				worker.runMatch(nm);
			}
		}
		em.close();
	}

	public synchronized void sendWorkerMatchDependencies(WorkerRepr worker, NetworkMatch match, boolean needMap, boolean needTeamA, boolean needTeamB) {
		Dependencies dep;
		byte[] map = null;
		byte[] teamA = null;
		byte[] teamB = null;
		try {
			if (needMap) {
				map = Util.getFileData(config.install_dir + "/battlecode/maps/" + match.map.getMapName() + ".xml");
			}
			if (needTeamA) {
				teamA = Util.getFileData(config.install_dir + "/battlecode/teams/" + match.team_a + ".jar");
			}
			if (needTeamB) {
				teamB = Util.getFileData(config.install_dir + "/battlecode/teams/" + match.team_b + ".jar");
			}
			dep = new Dependencies(match.map.getMapName(), map, match.team_a, teamA, match.team_b, teamB);
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
		BSRun nextRun = null;
		try {
			nextRun = em.createQuery("from BSRun run where run.status = ? order by run.id asc limit 1", BSRun.class).setParameter(1, BSRun.STATUS.QUEUED).getSingleResult();
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
		BSRun currentRun = null;
		try {
			currentRun = em.createQuery("from BSRun run where run.status = ?", BSRun.class).setParameter(1, BSRun.STATUS.RUNNING).getSingleResult();
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
		
		// Remove unfinished matches
		em.getTransaction().begin();
		em.createQuery("delete from BSMatch m where m.run = ? and m.status != ?")
		.setParameter(1, currentRun)
		.setParameter(2, BSMatch.STATUS.FINISHED)
		.executeUpdate();
		em.flush();
		em.getTransaction().commit();
		em.close();
		wph.broadcastMsg("matches", new CometMessage(CometCmd.FINISH_RUN, new String[] {""+currentRun.getId(), ""+status}));
		wph.broadcastMsg("connections", new CometMessage(CometCmd.FINISH_RUN, new String[] {}));
		for (WorkerRepr c: workers) {
			c.stopAllMatches();
		}
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
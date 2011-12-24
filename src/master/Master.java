package master;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import model.BSMap;
import model.BSMatch;
import model.BSPlayer;
import model.BSRun;
import model.MatchResult;
import model.STATUS;
import model.TEAM;
import networking.Packet;

import common.Config;
import common.Dependencies;
import common.HibernateUtil;
import common.NetworkMatch;
import common.Util;


/**
 * Handles distribution of load to connected workers
 * @author stevearc
 *
 */
public class Master {
	private Logger _log;
	private NetworkHandler handler;
	private HashSet<WorkerRepr> workers = new HashSet<WorkerRepr>();
	private Date mapsLastModifiedDate;
	private File pendingBattlecodeServerFile;
	private File pendingIdataFile;
	private File pendingBuildFile;
	private File pendingConfFile;
	private boolean initialized;

	public Master() throws Exception {
		_log = Config.getLogger();
		handler = new NetworkHandler();
	}

	/**
	 * Start running the master
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	public synchronized void start() throws NoSuchAlgorithmException, IOException {
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
		WebSocketChannelManager.startHeartbeatManager();
		initialized = true;
	}
	
	public synchronized boolean isInitialized() {
		return initialized;
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
		newRun.setaWins(0l);
		newRun.setbWins(0l);
		newRun.setStatus(STATUS.QUEUED);
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
		WebSocketChannelManager.broadcastMsg("index", "INSERT_TABLE_ROW", newRun.getId() + "," + 
				teamA.getPlayerName() + "," +  teamB.getPlayerName());
		startRun();
		_log.info("Queued new run: " + newRun);
	}

	public synchronized void updateBattlecodeFiles(File battlecode_server, File idata, File build, File bc_conf) {
		pendingBattlecodeServerFile = battlecode_server;
		pendingIdataFile = idata;
		pendingBuildFile = build;
		pendingConfFile = bc_conf;
		if (getCurrentRun() == null) {
			writeBattlecodeFiles();
		}
	}
	
	private void writeBattlecodeFiles() {
		try {
			if (pendingBattlecodeServerFile != null && pendingIdataFile != null) {
				_log.info("Writing updated battlecode files");
				Util.writeFileData(pendingBattlecodeServerFile, "./battlecode/lib/battlecode-server.jar");
				Util.writeFileData(pendingIdataFile, "./battlecode/idata");
				Util.writeFileData(pendingBuildFile, "./battlecode/build.xml");
				Util.writeFileData(pendingConfFile, "./battlecode/bc.conf");
				pendingBattlecodeServerFile = null;
				pendingIdataFile = null;
				pendingBuildFile = null;
				pendingConfFile = null;
			}
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Error updating battlecode version", e);
		}
	}

	/**
	 * Delete run data
	 * @param runId
	 */
	public synchronized void deleteRun(Long runId) {
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun run = em.find(BSRun.class, runId);
		// If it's running right now, just cancel it
		if (run.getStatus() == STATUS.RUNNING) {
			_log.info("canceling run");
			stopCurrentRun(STATUS.CANCELED);
			startRun();
		} else {
			// otherwise, delete it
			// first delete rms files
			for (BSMatch match: run.getMatches()) {
				if (match.getStatus() == BSMatch.STATUS.FINISHED) {
					File matchFile = new File("./matches/" + match.toMatchFileName());
					if (matchFile.exists()) {
						matchFile.delete();
					}
				}
			}
			
			// Then delete database entries
			em.getTransaction().begin();
			em.remove(run);
			em.flush();
			em.getTransaction().commit();
			em.close();
			WebSocketChannelManager.broadcastMsg("index", "DELETE_TABLE_ROW", ""+runId);
		}
	}

	/**
	 * Process a worker connecting
	 * @param worker
	 */
	public synchronized void workerConnect(WorkerRepr worker) {
		_log.info("Worker connected: " + worker);
		workers.add(worker);
		WebSocketChannelManager.broadcastMsg("connections", "INSERT_TABLE_ROW", worker.toHTML());
	}

	/**
	 * Process a worker disconnecting
	 * @param worker
	 */
	public synchronized void workerDisconnect(WorkerRepr worker) {
		_log.info("Worker disconnected: " + worker);
		WebSocketChannelManager.broadcastMsg("connections", "DELETE_TABLE_ROW", worker.toHTML());
		workers.remove(worker);
	}

	/**
	 * Save data from a finished match
	 * @param worker
	 * @param p
	 */
	public synchronized void matchFinished(WorkerRepr worker, Packet p) {
		NetworkMatch m = (NetworkMatch) p.get(0);
		BSMatch.STATUS status = (BSMatch.STATUS) p.get(1);
		MatchResult result = (MatchResult) p.get(2);
		byte[] data = (byte[]) p.get(3);
		WebSocketChannelManager.broadcastMsg("connections", "REMOVE_MAP", worker.toHTML() + "," + m.toMapString());
		try {
			EntityManager em = HibernateUtil.getEntityManager();
			BSMatch match = em.find(BSMatch.class, m.id);
			if (match.getStatus() != BSMatch.STATUS.RUNNING || match.getRun().getStatus() != STATUS.RUNNING) {
				// Match was already finished by another worker
			} else if (status == BSMatch.STATUS.FINISHED) {
				em.getTransaction().begin();
				em.persist(result);
				em.flush();
				em.getTransaction().commit();
				match.setResult(result);
				match.setStatus(BSMatch.STATUS.FINISHED);
				BSRun run = match.getRun();
				if (match.getResult().getWinner() == TEAM.A) {
					run.setaWins(run.getaWins() + 1);
				} else {
					run.setbWins(run.getbWins() + 1);
				}
				_log.info("Match finished: " + m + " winner: " + result.getWinner());
				File outFile = new File("./static/matches/" + match.getRun().getId() + match.getMap().getMapName() + m.seed + ".rms");
				outFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(outFile);
				fos.write(data);
				em.getTransaction().begin();
				em.merge(match);
				em.merge(run);
				em.flush();
				em.getTransaction().commit();
				
				// Calculate percent finished and find win status
				String winRecord = run.getaWins() + "/" + run.getbWins();
				List<Object[]> resultList = em.createQuery("select match.status, count(*) from BSMatch match where match.run.status = ? group by match.status", Object[].class)
				.setParameter(1, STATUS.RUNNING)
				.getResultList();
				long currentMatches = 0;
				long totalMatches = 0;
				for (Object[] valuePair: resultList) {
					if (valuePair[0] == BSMatch.STATUS.FINISHED) {
						currentMatches += (Long) valuePair[1];
					}
					totalMatches += (Long) valuePair[1];
				}
				String percent = currentMatches*100/totalMatches + "%";
				WebSocketChannelManager.broadcastMsg("index", "MATCH_FINISHED", m.run_id + "," + 
						percent + "," + winRecord);
			} else {
				_log.warning("Match " + m + " on worker " + worker + " failed");
			}

			Long matchesLeft = em.createQuery("select count(*) from BSMatch match where match.run = ? and match.status != ?", Long.class)
			.setParameter(1, match.getRun())
			.setParameter(2, BSMatch.STATUS.FINISHED)
			.getSingleResult();
			// If finished, start next run
			if (matchesLeft == 0) {
				stopCurrentRun(STATUS.COMPLETE);
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
		if (getCurrentRun() == null) {
			return;
		}
		EntityManager em = HibernateUtil.getEntityManager();
		List<BSMatch> queuedMatches = em.createQuery("from BSMatch match inner join fetch match.map inner join fetch match.run " +
				"where match.status = ? and match.run.status = ?", BSMatch.class)
		.setParameter(1, BSMatch.STATUS.QUEUED)
		.setParameter(2, STATUS.RUNNING)
		.getResultList();
		String battlecodeServerHash;
		String idataHash;
		String buildHash;
		String confHash;
		try {
			battlecodeServerHash = Util.convertToHex(Util.SHA1Checksum("./battlecode/lib/battlecode-server.jar"));
			idataHash = Util.convertToHex(Util.SHA1Checksum("./battlecode/idata"));
			buildHash = Util.convertToHex(Util.SHA1Checksum("./battlecode/build.xml"));
			confHash = Util.convertToHex(Util.SHA1Checksum("./battlecode/bc.conf"));
		} catch (NoSuchAlgorithmException e) {
			_log.log(Level.SEVERE, "Cannot find SHA1 algorithm!", e);
			return;
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Error hashing battlecode-server.jar or idata", e);
			return;
		}

		for (BSMatch m: queuedMatches) {
			if (!worker.isFree())
				break;
			NetworkMatch nm = m.buildNetworkMatch();
			nm.battlecodeServerHash = battlecodeServerHash;
			nm.idataHash = idataHash;
			nm.buildHash = buildHash;
			nm.confHash = confHash;
			_log.info("Sending match " + nm + " to worker " + worker);
			WebSocketChannelManager.broadcastMsg("connections", "ADD_MAP", worker.toHTML() + "," + nm.toMapString());
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
			List<BSMatch> runningMatches = em.createQuery("from BSMatch match inner join fetch match.map inner join fetch match.run " +
					"where match.status = ? and match.run.status = ?", BSMatch.class)
			.setParameter(1, BSMatch.STATUS.RUNNING)
			.setParameter(2, STATUS.RUNNING)
			.getResultList();
			while (!runningMatches.isEmpty() && worker.isFree() && 
					worker.getRunningMatches().size() < runningMatches.size()) {
				BSMatch m = null;
				NetworkMatch nm = null;
				do {
					m = runningMatches.get(r.nextInt(runningMatches.size()));
					nm = m.buildNetworkMatch();
				} while (worker.getRunningMatches().contains(nm));
				nm.battlecodeServerHash = battlecodeServerHash;
				nm.idataHash = idataHash;
				nm.buildHash = buildHash;
				nm.confHash = confHash;
				_log.info("Sending redundant match " + nm + " to worker " + worker);
				WebSocketChannelManager.broadcastMsg("connections", "ADD_MAP", worker.toHTML() + "," + nm.toMapString());
				worker.runMatch(nm);
			}
		}
		em.close();
	}

	public synchronized void sendWorkerMatchDependencies(WorkerRepr worker, NetworkMatch match, boolean needUpdate, boolean needMap, boolean needTeamA, boolean needTeamB) {
		Dependencies dep;
		byte[] map = null;
		byte[] teamA = null;
		byte[] teamB = null;
		byte[] battlecodeServer = null;
		byte[] idata = null;
		byte[] build = null;
		byte[] bc_conf = null;
		try {
			if (needUpdate) {
				battlecodeServer = Util.getFileData("./battlecode/lib/battlecode-server.jar");
				idata = Util.getFileData("./battlecode/idata");
				build = Util.getFileData("./battlecode/build.xml");
				bc_conf = Util.getFileData("./battlecode/bc.conf");
			}
			if (needMap) {
				map = Util.getFileData("./battlecode/maps/" + match.map.getMapName() + ".xml");
			}
			if (needTeamA) {
				teamA = Util.getFileData("./battlecode/teams/" + match.team_a + ".jar");
			}
			if (needTeamB) {
				teamB = Util.getFileData("./battlecode/teams/" + match.team_b + ".jar");
			}
			dep = new Dependencies(battlecodeServer, idata, build, bc_conf, match.map.getMapName(), map, match.team_a, teamA, match.team_b, teamB);
			_log.info("Sending " + worker + " " + dep);
			worker.runMatchWithDependencies(match, dep);
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Could not read data file", e);
			WebSocketChannelManager.broadcastMsg("connections", "REMOVE_MAP", worker.toHTML() + "," + match.toMapString());
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
			List<BSRun> queued = em.createQuery("from BSRun run where run.status = ? order by run.id asc limit 1", BSRun.class)
			.setParameter(1, STATUS.QUEUED)
			.getResultList();
			if (queued.size() > 0) {
				nextRun = queued.get(0);
			}
		} catch (NoResultException e) {
			// pass
		}
		if (nextRun != null) {
			nextRun.setStatus(STATUS.RUNNING);
			nextRun.setStarted(new Date());
			em.merge(nextRun);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			WebSocketChannelManager.broadcastMsg("index", "START_RUN", nextRun.getId() + "");
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
			currentRun = em.createQuery("from BSRun run where run.status = ?", BSRun.class).setParameter(1, STATUS.RUNNING).getSingleResult();
		} catch (NoResultException e) {
			// pass
		} finally {
			em.close();
		}
		return currentRun;
	}

	private void stopCurrentRun(STATUS status) {
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
		WebSocketChannelManager.broadcastMsg("index", "FINISH_RUN", currentRun.getId() + "," + status.toString());
		WebSocketChannelManager.broadcastMsg("connections", "FINISH_RUN", currentRun.getId() + "");
		for (WorkerRepr c: workers) {
			c.stopAllMatches();
		}
		writeBattlecodeFiles();
	}

	/**
	 * Update the list of available maps
	 */
	public synchronized void updateMaps() {
		File file = new File("battlecode/maps");
		if (new Date(file.lastModified()).equals(mapsLastModifiedDate))
			return;
		mapsLastModifiedDate = new Date(file.lastModified());
		File[] mapFiles = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		ArrayList<BSMap> newMaps = new ArrayList<BSMap>();
		if (mapFiles == null)
			return;
		for (File m: mapFiles) {
			try {
				newMaps.add(new BSMap(m));
			} catch (Exception e) {
				_log.log(Level.WARNING, "Error parsing map", e);
			}
		}

		EntityManager em = HibernateUtil.getEntityManager();
		
		List<String> mapNames = em.createQuery("select map.mapName from BSMap map", String.class).getResultList();
		for (BSMap map: newMaps) {
			if (!mapNames.contains(map.getMapName())) {
				em.persist(map);
				WebSocketChannelManager.broadcastMsg("index", "ADD_MAP", map.getMapName());
			}
		}
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
		em.close();
	}

	/**
	 * 
	 * @return Currently connected workers
	 */
	public HashSet<WorkerRepr> getConnections() {
		return workers;
	}

}
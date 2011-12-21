package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;

import master.Master;
import model.BSMatch;
import model.BSPlayer;
import model.BSRun;
import model.BSUser;
import model.MatchResult;
import model.STATUS;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import web.ProxyServer;
import web.WebServer;
import worker.GameData;
import worker.Worker;

import common.Config;
import common.HibernateUtil;
import common.Util;


/**
 * Starts all threads and services
 * @author stevearc
 *
 */

public class Main {
	private static final String HTTP_PORT_OP = "http-port";
	private static final String SSL_PORT_OP = "ssl-port";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		HelpFormatter formatter = new HelpFormatter();
		options.addOption("s", "server", false, "run as server");
		options.addOption("w", "worker", false, "run as worker");
		if (Config.DEBUG) {
			options.addOption("a", "analyze", true, "analyze a match file");
			options.addOption("p", "populate", false, "populate the server DB with mock data");
			options.addOption("m", "mock-worker", false, "run as a mock-worker");
			options.addOption("n", "mock-worker-sleep", true, "when running as mock-worker, time in seconds to take per match");
		}
		options.addOption("h", "help", false, "display help text");
		options.addOption("o", HTTP_PORT_OP, true, "what port for the http server to listen on (default 80)");
		options.addOption("l", SSL_PORT_OP, true, "what port for the https server to listen on (default 443)");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e1) {
			formatter.printHelp("run.sh", options);
			return;
		}
		if (cmd.hasOption('h')) {
			formatter.printHelp("run.sh", options);
			return;
		}

		try {
			// -s means Start the server
			if (cmd.hasOption('s')) {
				Config config = new Config(true);
				Config.setConfig(config);
				if (cmd.hasOption(HTTP_PORT_OP)) {
					config.http_port = Integer.parseInt(cmd.getOptionValue(HTTP_PORT_OP));
				}
				if (cmd.hasOption(SSL_PORT_OP)) {
					config.https_port = Integer.parseInt(cmd.getOptionValue(SSL_PORT_OP));
				}
				// If this is the first run, make sure the initial admin is in the DB
				if (!config.admin.trim().equals(""))
					createWebAdmin();

				Master m = new Master();
				Config.setMaster(m);
				new Thread(new ProxyServer()).start();
				new Thread(new WebServer()).start();
				m.start();
				if (cmd.hasOption('p')) {
					createMockData();
				}
			} // Start the worker
			else if (cmd.hasOption('w')){
				Config c = new Config(false);
				Config.setConfig(c);
				new Thread(new Worker()).start();
			} else if (cmd.hasOption('a')) {
				GameData gameData = new GameData(cmd.getOptionValue('a'));
				MatchResult result = gameData.analyzeMatch();
				System.out.println(result);
			} else if (cmd.hasOption('m')) {
				Config c = new Config(false);
				Config.setConfig(c);
				Config.MOCK_WORKER = true;
				if (cmd.hasOption('n')) {
					Config.MOCK_WORKER_SLEEP = Integer.parseInt(cmd.getOptionValue('n'));
				}
				c.cores = 4;
				new Thread(new Worker()).start();
			} else {
				System.out.println("Must specify if running as server or worker.  Do ./run.sh -h for help.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createMockData() throws IOException {
		Config.getConfig().getLogger().info("Populating database with mock data...");
		EntityManager em = HibernateUtil.getEntityManager();
		File checkFile = new File(Config.getConfig().install_dir + "/battlecode/teams/mock_player.jar");
		BSPlayer bsPlayer;
		if (!checkFile.exists()) {
			checkFile.createNewFile();
			bsPlayer = new BSPlayer();
			bsPlayer.setPlayerName("mock_player");
			em.persist(bsPlayer);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			em.refresh(bsPlayer);
		} else {
			bsPlayer = em.createQuery("from BSPlayer", BSPlayer.class).getResultList().get(0);
		}

		List<Long> seeds = new ArrayList<Long>();
		Random r = new Random();
		for (int i = 0; i < 10; i++) {
			seeds.add(r.nextLong());
		}
		while (!Config.getMaster().isInitialized()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		Config.getMaster().updateMaps();
		List<Long> mapIds = em.createQuery("select map.id from BSMap map", Long.class).getResultList();
		for (int i = 0; i < 5; i++) {
			Config.getMaster().queueRun(bsPlayer.getId(), bsPlayer.getId(), seeds, mapIds);
		}
		
		em.getTransaction().begin();
		List<BSRun> runs = em.createQuery("from BSRun", BSRun.class).getResultList();
		for (BSRun run: runs) {
			run.setStatus(STATUS.COMPLETE);
			run.setEnded(new Date(new Date().getTime() + 100000 + r.nextInt(100000000)));
			em.merge(run);
		}
		em.flush();
		em.getTransaction().commit();
		
		List<BSMatch> matches = em.createQuery("from BSMatch", BSMatch.class).getResultList();
		MatchResult mock;
		int i = 0;
		for (BSMatch m: matches) {
			mock = MatchResult.constructMockMatchResult();
			m.setResult(mock);
			m.setStatus(BSMatch.STATUS.FINISHED);
			em.persist(mock);
			em.merge(m);
			if (i++ % 50 == 0) {
				em.getTransaction().begin();
				em.flush();
				em.getTransaction().commit();
			}
		}
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
		em.close();
		Config.getConfig().getLogger().info("Finished creating mock data!");
	}

	/**
	 * Take the initial user/pass from the config file and put the information into the DB
	 */
	private static void createWebAdmin() {
		EntityManager em = HibernateUtil.getEntityManager();

		Long numUsers = em.createQuery("select count(*) from BSUser", Long.class).getSingleResult();
		if (numUsers == 0) {

			Config config = Config.getConfig();
			String salt = Util.SHA1(""+Math.random());
			String hashed_password = Util.SHA1(config.admin_pass + salt);
			BSUser user = new BSUser();
			user.setUsername(config.admin);
			user.setHashedPassword(hashed_password);
			user.setSalt(salt);
			user.setPrivs(BSUser.PRIVS.ADMIN);
			em.getTransaction().begin();
			em.merge(user);
			em.flush();
			em.getTransaction().commit();
			em.close();

			// Now remove the user information from the config file
			try {
				Runtime.getRuntime().exec(config.cmd_clean_config_file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

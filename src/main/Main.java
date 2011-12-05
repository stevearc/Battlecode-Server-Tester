package main;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

import master.Master;
import master.MasterMethodCaller;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import web.ProxyServer;
import web.WebServer;
import worker.Worker;
import beans.BSUser;

import common.Config;
import common.Util;

import db.HibernateUtil;

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
				MasterMethodCaller.startMaster();
				new Thread(new ProxyServer()).start();
				new Thread(new WebServer()).start();
			} // Start the worker
			else if (cmd.hasOption('w')){
				Config c = new Config(false);
				Config.setConfig(c);
				new Thread(new Worker()).start();
			} else {
				System.out.println("Must specify if running as server or worker.  Do ./run.sh -h for help.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Take the initial user/pass from the config file and put the information into the DB
	 */
	private static void createWebAdmin() {
		EntityManager em = HibernateUtil.getEntityManager();
		
		// TODO: rowCount
		List users = em.createQuery("from BSUser user").getResultList();
		if (users.isEmpty()) {
		
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

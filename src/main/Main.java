package main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

import common.Config;
import common.Util;

import db.Database;
import db.HSQLDatabase;
import db.MySQLDatabase;

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
		options.addOption("h", "help", false, "display help text");
		options.addOption("o", HTTP_PORT_OP, true, "what port for the http server to listen on (default 80)");
		options.addOption("l", SSL_PORT_OP, true, "what port for the https server to listen on (default 443)");
		options.addOption("r", "reset-db", false, "clear and re-create the database");

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
				config.reset_db = cmd.hasOption('r');
				Database db = null;
				if (config.db_type.equals("mysql")){
					db = new MySQLDatabase();
				} else if (config.db_type.equals("hsql")) {
					db = new HSQLDatabase();
				} else {
					throw new Exception("Invalid database type");
				}
				db.connect();
				Config.setDB(db);
				// If this is the first run, make sure the initial admin is in the DB
				if (!config.admin.trim().equals(""))
					createWebAdmin();

				Master m = new Master();
				Config.setMaster(m);
				MasterMethodCaller.startMaster();
				new Thread(new ProxyServer()).start();
				new Thread(new WebServer()).start();
			} // Start the worker
			else {
				Config c = new Config(false);
				Config.setConfig(c);
				new Thread(new Worker()).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Take the initial user/pass from the config file and put the information into the DB
	 * @throws SQLException
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private static void createWebAdmin() throws SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {
		Database db = Config.getDB();
		Config config = Config.getConfig();
		String salt = Util.SHA1(""+Math.random());
		String hashed_password = Util.SHA1(config.admin_pass + salt);
		PreparedStatement st = db.prepare("INSERT INTO users (username, password, salt, status) " +
		"VALUES (?, ?, ?, ?)");
		st.setString(1, config.admin);
		st.setString(2, hashed_password);
		st.setString(3, salt);
		st.setInt(4, 2);
		db.update(st, false);

		// Now remove the user information from the config file
		try {
			Runtime.getRuntime().exec(config.cmd_clean_config_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

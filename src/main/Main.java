package main;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import web.WebServer;
import backend.Server;
import backend.ServerMethodCaller;
import client.Client;

import common.Config;

import db.Database;
import db.HSQLDatabase;
import db.MySQLDatabase;

/**
 * Where everything starts
 * @author steven
 *
 */

public class Main {
	private static final String VERSION = "0.2";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		HelpFormatter formatter = new HelpFormatter();
		options.addOption("s", "server", false, "run as server");
		options.addOption("h", "help", false, "display help text");
		options.addOption("v", "version", false, "display version");
		options.addOption("o", "http-port", true, "what port for the http server to listen on (default 80)");
		options.addOption("l", "ssl-port", true, "what port for the https server to listen on (default 443)");
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
		if (cmd.hasOption('v')) {
			System.out.println("Version: " + VERSION);
			return;
		}


		try {
			if (cmd.hasOption('s')) {
				Config c = new Config(true);
				Config.setConfig(c);
				if (cmd.hasOption('o')) {
					c.http_port = Integer.parseInt(cmd.getOptionValue('o'));
				}
				if (cmd.hasOption('l')) {
					c.https_port = Integer.parseInt(cmd.getOptionValue('l'));
				}
				c.reset_db = cmd.hasOption('r');
				Database db = null;
				if (c.db_type.equals("mysql")){
					db = new MySQLDatabase();
				} else if (c.db_type.equals("hsql")) {
					db = new HSQLDatabase();
				} else {
					throw new Exception("Invalid database type");
				}
				db.connect();
				Config.setDB(db);
				Server s = new Server();
				Config.setServer(s);
				ServerMethodCaller.pokeServer();
				new Thread(new WebServer()).start();
			}
			else {
				Config c = new Config(false);
				Config.setConfig(c);
				new Thread(new Client()).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

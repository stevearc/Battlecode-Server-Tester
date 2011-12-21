package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import master.Master;
import master.WebPollHandler;

/**
 * Handles the parsing of bs-tester.conf and stores
 * all the data in one convenient container
 * @author stevearc
 *
 */
public class Config {
	public static final int RESTART_STATUS = 121;
	public static final boolean DEBUG = true;
	public static final boolean SHOW_SQL = true;
	public static boolean MOCK_WORKER = false;
	public static int MOCK_WORKER_SLEEP = 0;
	private static Config rootConfig;
	private static Master rootMaster;
	private static WebPollHandler webPollHandler;

	/* These options must be changed in source */
	public long timeout = 300000;

	/* These options are only specified on the command line */
	private boolean isServer;
	public int http_port = 80;
	private static final String log_dir = "./log";

	/* These options are specified in the battlecode.conf file */
	/** The directory the application is installed into */
	public String install_dir = "";
	/** The port number of the server to connect to (or listen on, if running as server) */
	public int port = 8888;
	/** WORKER ONLY: The internet address of the master to connect to */
	public String server = "";
	/** WORKER ONLY: The number of simultaneous matches to run at a time */
	public int cores = 1;
	/** MASTER ONLY: The cutoff value for a map's area for the map to be considered "small" */
	public int map_cutoff_small = 1400;
	/** MASTER ONLY: The cutoff value for a map's area for the map to be considered "medium" */
	public int map_cutoff_medium = 2400;

	/* These options are generated from the above options */
	/** The path to the script that will generate the proper bc.conf file */
	public String cmd_gen_conf = "./scripts/gen_conf.sh";
	/** The path to the script that will rename the team in the source */
	public String cmd_rename_team = "./scripts/rename_team.sh";
	/** The path to the script that runs the battlecode match */
	public String cmd_run_match = "./scripts/run_match.sh";
	/** The path to the script that will remove the ADMIN and ADMIN_PASS fields from the config file */
	public String cmd_clean_config_file = "./scripts/clean_config_file.sh";

	/* These are special case options.  They exist initially but are deleted as soon as they are
	 * put into the database.  They start in the config file to make setup easier */
	/** The initial web admin username */
	public String admin = "";
	/** The initial web admin password */
	public String admin_pass = "";

	public Config(boolean isMaster) throws IOException {
		this.isServer = isMaster;
		File file = new File("./etc/bs-tester.conf");
		if (!file.exists()) {
			if (!file.exists()) {
				throw new IOException("Config file ./etc/bs-tester.conf does not exist.  Make sure you have run setup.sh");
			}
		}
		File logDir = new File(log_dir);
		if (!logDir.exists()) {
			System.out.println("Creating log dir: " + log_dir);
			logDir.mkdirs();
		}
		FileInputStream f = new FileInputStream(file);
		BufferedReader read = new BufferedReader(new InputStreamReader(f));
		for (String line = read.readLine(); line != null; line = read.readLine()) {
			int comment = line.indexOf("#");
			if (comment != -1)
				line = line.substring(0,comment).trim();
			if ("".equals(line))
				continue;
			String[] data = line.split("=");
			if (data.length == 2)
				parse(data[0], data[1]);
		}
		validate();
		setWebPollHandler(new WebPollHandler());
	}

	public static Config getConfig() {
		return rootConfig;
	}

	public static void setConfig(Config c) {
		rootConfig = c;
	}

	// TODO: remove references to getMaster
	public static Master getMaster() {
		return rootMaster;
	}

	public static void setMaster(Master s) {
		rootMaster = s;
	}

	public static WebPollHandler getWebPollHandler() {
		return webPollHandler;
	}

	private static void setWebPollHandler(WebPollHandler wph) {
		webPollHandler = wph;
	}

	public Logger getLogger() {
		if (isServer) {
			Logger logger = Logger.getLogger("Master");
			logger.setLevel(Level.ALL);
			try {
				if (logger.getHandlers().length < 2) {
					FileHandler master_handler = new FileHandler(log_dir + "/bs-master.log");
					master_handler.setFormatter(new SimpleFormatter());
					logger.addHandler(master_handler);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return logger;
		} else {
			Logger logger = Logger.getLogger("Worker");
			logger.setLevel(Level.ALL);
			try {
				if (logger.getHandlers().length < 2) {
					FileHandler worker_handler = new FileHandler(log_dir + "/bs-worker.log");
					worker_handler.setFormatter(new SimpleFormatter());
					logger.addHandler(worker_handler);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return logger;
		}
	}

	/**
	 * Assigns the values of a parameter in the conf file to variables in Config
	 * @param option
	 * @param value
	 */
	private void parse(String option, String value) {
		option = option.toLowerCase();
		if (option.equals("install_dir")) {
			install_dir = value;
		}
		else if (option.equals("admin")) {
			admin = value;
		}
		else if (option.equals("admin_pass")) {
			admin_pass = value;
		}
		else if (option.equals("server")) {
			server = value;
		}
		else if (option.equals("port")) {
			port = Integer.parseInt(value);
		} 
		else if (option.equals("map_cutoff_small")) {
			map_cutoff_small = Integer.parseInt(value);
		} 
		else if (option.equals("map_cutoff_medium")) {
			map_cutoff_medium = Integer.parseInt(value);
		} 
		else if (option.equals("repo_addr")) {
			// pass
		}
		else if (option.equals("cores")) {
			cores = Integer.parseInt(value);
		}
		else if (option.equals("team")) {
			// This is only used in the commandline scripts
		}
		else {
			System.err.println("Unrecognized option: " + option);
		}
	}

	/**
	 * Check to see if the config values are valid
	 * @throws InvalidConfigException
	 */
	private void validate() throws InvalidConfigException {
		// Check the server and worker values
		if (!new File(install_dir).exists())
			throw new InvalidConfigException("Invalid install directory " + install_dir);

		if (port < 1 || port > 65535)
			throw new InvalidConfigException("Invalid port number " + port);

		// Check the worker values
		if (!isServer) {
			if (server.equals(""))
				throw new InvalidConfigException("Invalid server address " + server);

			if (cores < 1) 
				throw new InvalidConfigException("Invalid number of cores: " + cores);
		}

		// Check the server values
		if (isServer) {
			if (map_cutoff_medium < 0)
				throw new InvalidConfigException("MAP_CUTOFF_MEDIUM must be positive");

			if (map_cutoff_small < 0)
				throw new InvalidConfigException("MAP_CUTOFF_SMALL must be positive");

			if (map_cutoff_medium <= map_cutoff_small)
				throw new InvalidConfigException("MAP_CUTOFF_SMALL must be less than MAP_CUTOFF_MEDIUM");
		}

	}

}

class InvalidConfigException extends IOException {
	private static final long serialVersionUID = -5660377810876368215L;
	public InvalidConfigException(String msg) {
		super (msg);
	}
}
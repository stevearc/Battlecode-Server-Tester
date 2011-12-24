package common;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import master.Master;

/**
 * Stores configuration data in one convenient container
 * @author stevearc
 *
 */
public class Config {
	public static final String VERSION = "0.1.0";
	public static final int RESTART_STATUS = 121;
	public static final boolean DEBUG = false;
	public static final boolean SHOW_SQL = false;
	public static final boolean PRINT_WORKER_OUTPUT = true;
	public static boolean MOCK_WORKER = false;
	public static int MOCK_WORKER_SLEEP = 0;
	private static Master rootMaster;
	private static final String log_dir = "./log";

	public static boolean isServer;
	public static int http_port = 80;

	/** The port number of the server to connect to (or listen on, if running as server) */
	public static int dataPort = 8888;
	/** WORKER ONLY: The internet address of the master to connect to */
	public static String server = "";
	/** WORKER ONLY: The number of simultaneous matches to run at a time */
	public static int cores = 1;
	/** MASTER ONLY: The cutoff value for a map's area for the map to be considered "small" */
	public static int map_cutoff_small = 1400;
	/** MASTER ONLY: The cutoff value for a map's area for the map to be considered "medium" */
	public static int map_cutoff_medium = 2400;

	/* These options are generated from the above options */
	/** The path to the script that will generate the proper bc.conf file */
	public static final String cmd_gen_conf = "./scripts/gen_conf.sh";
	/** The path to the script that will rename the team in the source */
	public static final String cmd_rename_team = "./scripts/rename_team.sh";
	/** The path to the script that runs the battlecode match */
	public static final String cmd_run_match = "./scripts/run_match.sh";

	// TODO: remove references to getMaster
	public static Master getMaster() {
		return rootMaster;
	}

	public static void setMaster(Master s) {
		rootMaster = s;
	}

	public static boolean initializedBattlecode() {
		File bserver = new File("./battlecode/lib/battlecode-server.jar");
		return bserver.exists();
	}

	public static Logger getLogger() {
		File logDir = new File(log_dir);
		if (!logDir.exists()) {
			System.out.println("Creating log dir: " + log_dir);
			logDir.mkdirs();
		}
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
}
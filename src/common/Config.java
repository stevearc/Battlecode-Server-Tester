package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Handles the parsing of battlecode.conf and stores
 * all the data in one convenient container
 * @author steven
 *
 */
public class Config {
	private boolean isServer;
	private String log_dir = "";
	/** The location of the repository being used */
	public String repo = "";
	/** CLIENT ONLY: The internet address of the server to connect to */
	public String server = "";
	/** The port number of the server to connect to (or listen on, if running as server) */
	public int port = 0;
	/** SERVER ONLY: The location to store all the match files. */
	public String matches = "";
	/** SERVER ONLY: The address of the MYSQL database to connect to. */
	public String db_addr = "";
	/** SERVER ONLY: The username to use when connecting to the MYSQL database. */
	public String db_user = "";
	/** SERVER ONLY: The password to use when connecting to the MYSQL database. */
	public String db_pass = "";
	/** The name of the user that owns the repository folder */
	public String user = "";
	/** The path to the script that will update the repository */
	public String cmd_update = "";
	/** The path to the script that will retrieve the appropriate teams from repository history */
	public String cmd_grabole = "";
	/** The path to the script that will generate the proper bc.conf file */
	public String cmd_gen_conf = "";
	/** The version control program being used (currently supports git and svn) */
	public String version_control = "";
	/** Whether or not to print out stack traces of exceptions */
	public boolean DEBUG = false;
	/** CLIENT ONLY: The number of simultaneous matches to run at a time */
	public int cores = 0;
	/** Time, in seconds, to wait before assuming match failed to run properly */
	public int timeout = 0;

	public Config(boolean isServer) throws IOException {
		this.isServer = isServer;
		File file = new File("/etc/battlecode.conf");
		if (!file.exists()) {
			file = new File("etc/battlecode.conf");
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
			parse(data[0], data[1]);
		}
		validate();
	}

	/**
	 * Assigns the values of a parameter in the conf file to variables in Config
	 * @param option
	 * @param value
	 */
	private void parse(String option, String value) {
		option = option.toLowerCase();
		if (option.equals("repo")) {
			repo = value;
		}
		else if (option.equals("log_dir")) {
			log_dir = value;
		}
		else if (option.equals("server")) {
			server = value;
		}
		else if (option.equals("port")) {
			port = Integer.parseInt(value);
		} 
		else if (option.equals("matches")) {
			matches = value;
		}
		else if (option.equals("db_user")) {
			db_user = value;
		}
		else if (option.equals("db_pass")) {
			db_pass = value;
		}
		else if (option.equals("database")) {
			db_addr = "jdbc:mysql://localhost/" + value;
		}
		else if (option.equals("debug")) {
			DEBUG = (Integer.parseInt(value) != 0);
		}
		else if (option.equals("team")) {
			// Do nothing
		}
		else if (option.equals("authorized_users")) {
			// Do nothing
		}
		else if (option.equals("user")) {
			user = value;
		}
		else if (option.equals("timeout")) {
			timeout = Integer.parseInt(value);
		}
		else if (option.equals("version_control")) {
			version_control = value.toLowerCase();
			cmd_gen_conf = "./scripts/" + value + "/gen_conf.sh";
			cmd_grabole = "./scripts/" + value + "/grabole.sh";
			cmd_update = "./scripts/" + value + "/update.sh";
		}
		else if (option.equals("cores")) {
			cores = Integer.parseInt(value);
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
		// Check the server and client values
		if (!new File(log_dir).exists())
			throw new InvalidConfigException("Invalid log directory " + log_dir);

		if (!new File(repo).exists())
			throw new InvalidConfigException("Invalid repository location " + repo);

		if (port < 1 || port > 65535)
			throw new InvalidConfigException("Invalid port number " + port);

		if (user.equals(""))
			throw new InvalidConfigException("Invalid repository owner " + user);

		if (!version_control.equals("svn") && !version_control.equals("git"))
			throw new InvalidConfigException("Invalid version control " + version_control);

		if (timeout < 60) 
			throw new InvalidConfigException("Invalid timeout parameter (min 60): " + timeout);

		// Check the client values
		if (!isServer) {
			if (server.equals(""))
				throw new InvalidConfigException("Invalid server address " + server);

			if (cores < 1) 
				throw new InvalidConfigException("Invalid number of cores: " + cores);
		}

		// Check the server values
		if (isServer) {
			if (matches.equals(""))
				throw new InvalidConfigException("Invalid match directory " + matches);

			if (!db_addr.matches("jdbc:mysql://\\w+/\\w+"))
				throw new InvalidConfigException("MYSQL address much match the format: jdbc:mysql://server/database");

			if (db_user.equals(""))
				throw new InvalidConfigException("Invalid MYSQL database user");

			if (db_pass.equals(""))
				throw new InvalidConfigException("Must have a non-blank MYSQL database password");
		}

	}

	/**
	 * Get the Logger for the Client to use
	 * @return The Logger object the Client uses
	 */
	public Logger getClientLogger() {
		Logger logger = Logger.getLogger("Client");
		try {
			if (logger.getHandlers().length < 2) {
				FileHandler client_handler = new FileHandler(log_dir + "/battlecode.txt");
				client_handler.setFormatter(new SimpleFormatter());
				logger.addHandler(client_handler);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logger;
	}

	/**
	 * Get the Logger for the server to use
	 * @return The Logger object the Server uses
	 */
	public Logger getServerLogger() {
		Logger logger = Logger.getLogger("Server");
		try {
			if (logger.getHandlers().length < 2) {
				FileHandler server_handler = new FileHandler(log_dir + "/battlecode_srv.txt");
				server_handler.setFormatter(new SimpleFormatter());
				logger.addHandler(server_handler);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logger;
	}

}

class InvalidConfigException extends IOException {
	private static final long serialVersionUID = -5660377810876368215L;
	public InvalidConfigException(String msg) {
		super (msg);
	}
}
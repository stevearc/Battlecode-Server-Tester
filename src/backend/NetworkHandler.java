package backend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Config;

/**
 * Listens on the chosen port and adds all new incoming connections
 * to a retrievable queue.
 * @author steven
 *
 */
public class NetworkHandler implements Runnable {
	private ServerSocket serverSocket;
	private Config config;
	private Logger _log;

	/**
	 * 
	 * @param server The server that controls this NetworkHandler
	 * @param config The configuration parameters
	 */
	public NetworkHandler() {
		this.config = Config.getConfig();
		this._log = config.getLogger();
	}

	@Override
	/**
	 * Listen on a port for all incoming connection requests
	 */
	public void run() {
		try {
			serverSocket = new ServerSocket(config.port);
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Could not listen on port: " + config.port, e);
			System.exit(1);
		}
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				ClientRepr client = new ClientRepr(socket);
				client.start();
				Config.getServer().clientConnect(client);
			} catch (IOException e) {
				_log.log(Level.WARNING, "Accept connection failed", e);
			}
		}
	}

}

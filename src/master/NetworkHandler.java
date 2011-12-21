package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import common.Config;

/**
 * Listens on the chosen port for workers and spawns threads for new connections
 * @author stevearc
 *
 */
public class NetworkHandler implements Runnable {
	private ServerSocket serverSocket;
	private Config config;
	private Logger _log;
	private ServerSocketFactory ssf;

	/**
	 * 
	 * @param server The server that controls this NetworkHandler
	 * @param config The configuration parameters
	 */
	public NetworkHandler() throws Exception {
		this.config = Config.getConfig();
		this._log = config.getLogger();
		
		ssf = ServerSocketFactory.getDefault();
	}

	@Override
	/**
	 * Listen on a port for all incoming connection requests
	 */
	public void run() {
		try {
			serverSocket = ssf.createServerSocket(config.port);
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Could not listen on port: " + config.port, e);
			System.exit(1);
		}
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				WorkerRepr worker = new WorkerRepr(socket);
				worker.start();
				Config.getMaster().workerConnect(worker);
			} catch (IOException e) {
				_log.log(Level.WARNING, "Accept connection failed", e);
			}
		}
	}

}

package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Logger;

import networking.Controller;
import networking.Network;

import common.Config;

/**
 * Listens on the chosen port and adds all new incoming connections
 * to a retrievable queue.
 * @author steven
 *
 */
public class NetworkHandler implements Runnable {
	private Vector<Network> queue = new Vector<Network>();
	private Logger _log;
	private ServerSocket serverSocket;
	private boolean finished = false;
	private Controller server;
	private Config config;

	/**
	 * 
	 * @param server The server that controls this NetworkHandler
	 * @param config The configuration parameters
	 */
	public NetworkHandler(Controller server, Config config) {
		this.config = config;
		this.server = server;
		this._log = config.getServerLogger();
	}

	/**
	 * Get the newly connected Client
	 * @return A Network or null.
	 */
	public Network getNewClient() {
		if (queue.size() == 0)
			return null;
		Network net = queue.firstElement();
		queue.remove(0);
		return net;
	}

	@Override
	/**
	 * Listen on a port for all incoming connection requests
	 */
	public void run() {
		try {
			serverSocket = new ServerSocket(config.port);
		} catch (IOException e) {
			_log.severe("Could not listen on port: " + config.port);
			System.exit(1);
		}
		while (!finished) {
			try {
				Socket socket = serverSocket.accept();
				Network net = new Network(server, config, socket, _log);
				new Thread(net).start();
				queue.add(net);
			} catch (IOException e) {
				_log.warning("Accept connection failed");
			}
		}
	}

	/**
	 * Shut down the listener nicely
	 */
	public void stop() {
		finished = true;
		Socket socket;
		try {
			socket = new Socket("localhost", config.port);
			socket.close();
		} catch (UnknownHostException e) {
			_log.severe("Cannot find localhost?");
			if (config.DEBUG)
				e.printStackTrace();
		} catch (IOException e) {
			_log.severe("Error shutting down connection listener");
			if (config.DEBUG)
				e.printStackTrace();
			}
		for (Network net: queue) {
			net.stop();
		}
	}

}

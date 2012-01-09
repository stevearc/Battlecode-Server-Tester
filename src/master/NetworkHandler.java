package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

import org.apache.log4j.Logger;

/**
 * Listens on the chosen port for workers and spawns threads for new connections
 * @author stevearc
 *
 */
public class NetworkHandler implements Runnable {
	private static Logger _log = Logger.getLogger(NetworkHandler.class);
	private static int nextWorkerId = 1;
	private ServerSocket serverSocket;
	private int dataPort;
	private ServerSocketFactory ssf;

	/**
	 * 
	 * @param server The server that controls this NetworkHandler
	 * @param config The configuration parameters
	 */
	public NetworkHandler(int dataPort) throws Exception {
		this.dataPort = dataPort;
		ssf = ServerSocketFactory.getDefault();
	}
	
	public int getDataPort() {
		return dataPort;
	}

	@Override
	/**
	 * Listen on a port for all incoming connection requests
	 */
	public void run() {
		try {
			serverSocket = ssf.createServerSocket(dataPort);
		} catch (IOException e) {
			_log.fatal("Could not listen on port: " + dataPort, e);
			System.exit(1);
		}
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				WorkerRepr worker = new WorkerRepr(socket, nextWorkerId++);
				worker.start();
				AbstractMaster.kickoffWorkerConnect(worker);
			} catch (IOException e) {
				_log.warn("Accept connection failed", e);
			}
		}
	}

}

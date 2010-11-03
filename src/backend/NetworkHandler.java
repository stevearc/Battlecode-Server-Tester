package backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import common.Config;

/**
 * Listens on the chosen port and adds all new incoming connections
 * to a retrievable queue.
 * @author steven
 *
 */
public class NetworkHandler implements Runnable {
	private SSLServerSocket serverSocket;
	private Config config;
	private Logger _log;
	private SSLServerSocketFactory ssf;

	/**
	 * 
	 * @param server The server that controls this NetworkHandler
	 * @param config The configuration parameters
	 */
	public NetworkHandler() throws Exception {
		this.config = Config.getConfig();
		this._log = config.getLogger();
		
	    KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(new FileInputStream(config.keystore), config.keystore_pass.toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(keystore);

		SSLContext context = SSLContext.getInstance("TLS");
		TrustManager[] trustManagers = tmf.getTrustManagers();
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(keystore, config.keystore_pass.toCharArray());
		KeyManager[] keyManagers = kmf.getKeyManagers();
		
		context.init(keyManagers, trustManagers, null);
		
		ssf = context.getServerSocketFactory();
	}

	@Override
	/**
	 * Listen on a port for all incoming connection requests
	 */
	public void run() {
		try {
			serverSocket = (SSLServerSocket) ssf.createServerSocket(config.port);
			serverSocket.setNeedClientAuth(true);
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

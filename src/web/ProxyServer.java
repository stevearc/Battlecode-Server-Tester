package web;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import common.Config;

/**
 * Redirect traffic on port 80 to port 443 to enforce SSL
 * @author stevearc
 *
 */
public class ProxyServer implements Runnable {
	private Config config;
	private Logger _log;

	public ProxyServer() {
		config = Config.getConfig();
		_log = config.getLogger();
	}

	@Override
	public void run() {
		try {
			Server server = new Server();

			SelectChannelConnector connector0 = new SelectChannelConnector();
			connector0.setPort(config.http_port);
			connector0.setMaxIdleTime(30000);
			connector0.setRequestHeaderSize(8192);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			context.addServlet(new ServletHolder(new ProxyServlet()),"/");
			server.setHandler(context);

			server.setConnectors(new Connector[]{ connector0});
			server.start();
			server.join();

		} catch (Exception e) {
			_log.log(Level.SEVERE, "Error running webserver", e);
		}
	}

}

package web;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.MultiPartFilter;

import common.Config;

/**
 * Starts the threads that run the web server
 * @author stevearc
 *
 */
public class WebServer implements Runnable {
	private Config config;
	private Logger _log;
	/** Must put any new servlets in this array to load them into the web server */
	private AbstractServlet[] servlets = {
			new IndexServlet(),
			new ConnectionsServlet(),
			new DeleteServlet(),
			new MatchesServlet(),
			new RunServlet(),
			new AnalysisServlet(),
			new MatchesByMapServlet(),
			new LoginServlet(),
			new AdminServlet(),
			new LogoutServlet(),
			new AdminActionServlet(),
			new UploadServlet(),
			};

	public WebServer() {
		config = Config.getConfig();
		_log = config.getLogger();
	}
	
	public void run() {
		try {
			Server server = new Server();
			// This is a hack to prevent Jetty from using /dev/random, because that can
			// randomly cause it to take >30s to start
			HashSessionIdManager idManager = new HashSessionIdManager();
			idManager.setRandom(new Random());
			server.setSessionIdManager(idManager);
			
	        // Add the servlets
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");

			context.addServlet(new ServletHolder(new IndexServlet()),"/");
			for (AbstractServlet s: servlets)
				context.addServlet(new ServletHolder(s), "/" + s.name);
			
			context.addFilter(new FilterHolder(new MultiPartFilter()), "/" + UploadServlet.NAME, 1);
			
			// Add the cometd servlet
			ServletHolder sh = new ServletHolder(new CometServlet());
			context.addServlet(sh, "/comet/*");

			// Serve static javascript files
			ContextHandler jsContext = new ContextHandler();
			jsContext.setContextPath("/js");
			jsContext.setHandler(new StaticFileHandler("text/javascript", ".+\\.js", config.install_dir + "/js"));
			
			// Serve static css files
			ContextHandler cssContext = new ContextHandler();
			cssContext.setContextPath("/css");
			cssContext.setHandler(new StaticFileHandler("text/css", ".+\\.css", config.install_dir + "/css"));
			
			// Serve static image files
			ContextHandler imgContext = new ContextHandler();
			imgContext.setContextPath("/images");
			imgContext.setHandler(new ImageHandler("images"));
			
			// Serve static match files
			ContextHandler matchContext = new ContextHandler();
			matchContext.setContextPath("/matches");
			matchContext.setHandler(new StaticFileHandler("application/octet-stream", ".+\\.rms", config.install_dir + "/matches"));
			
			// Add contexts
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[] {jsContext, cssContext, imgContext, matchContext, context});
			server.setHandler(contexts);
			
			SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
			ssl_connector.setPort(config.https_port);
			ssl_connector.setKeystore(config.keystore);
			ssl_connector.setPassword(config.keystore_pass);
			
			server.setConnectors(new Connector[]{ssl_connector});
			server.start();
			server.join();
		} catch (Exception e) {
			_log.log(Level.SEVERE, "Error running webserver", e);
		}
	}
	
}

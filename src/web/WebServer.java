package web;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.HashSessionIdManager;
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
	private Logger _log = Config.getLogger();
	/** Must put any new servlets in this array to load them into the web server */
	private HttpServlet[] servlets = {
			new IndexServlet(),
			new ConnectionsServlet(),
			new DeleteServlet(),
			new MatchesServlet(),
			new RunServlet(),
			new MatchesByMapServlet(),
			new LoginServlet(),
			new AdminServlet(),
			new LogoutServlet(),
			new AdminActionServlet(),
			new UploadServlet(),
			new AnalysisServlet(),
			new AnalysisContentServlet(),
			new MyWebSocketServlet(),
			};

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

			for (HttpServlet s: servlets) {
				context.addServlet(new ServletHolder(s), "/" + s.toString());
			}
			context.addServlet(new ServletHolder(new IndexServlet()), "/");

			EnumSet<DispatcherType> en = EnumSet.of(DispatcherType.REQUEST);
			// Set up filters for login blocking and handling file uploads
			context.addFilter(new FilterHolder(new LoginFilter()), "/*", en);
			context.addFilter(new FilterHolder(new MultiPartFilter()), "/" + UploadServlet.NAME, en);
			
			ResourceHandler resourceHandler = new FileHandler();
			resourceHandler.setAliases(true);
			resourceHandler.setResourceBase("static");
			
			// Add contexts
			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] {resourceHandler, context, new DefaultHandler()});
			server.setHandler(handlers);
			
			SelectChannelConnector connector = new SelectChannelConnector();
			connector.setPort(Config.http_port);
			server.addConnector(connector);
			server.start();
			server.join();
		} catch (Exception e) {
			_log.log(Level.SEVERE, "Error running webserver", e);
			_log.warning("You need root permissions to run the webserver on ports 80 and 443, or you can specify different ports.  " +
					"Run with -h for more details.");
			System.exit(1);
		}
	}
	
}

package web;

import java.io.File;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.MultiPartFilter;

/**
 * Starts the threads that run the web server
 * @author stevearc
 *
 */
public class WebServer implements Runnable {
	private Logger _log = Logger.getLogger(WebServer.class);
	private int httpPort;
	
	public WebServer(int httpPort) {
		this.httpPort = httpPort;
	}
	
	public void run() {
		try {
			_log.info("Starting WebServer");
			Server server = new Server();
			
	        // Add the servlets
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");

			context.addServlet(IndexServlet.class, "/");
			context.addServlet(IndexServlet.class, IndexServlet.NAME);
			context.addServlet(ConnectionsServlet.class, ConnectionsServlet.NAME);
			context.addServlet(DeleteServlet.class, DeleteServlet.NAME);
			context.addServlet(MatchWrapperServlet.class, MatchWrapperServlet.NAME);
			context.addServlet(MatchesServlet.class, MatchesServlet.NAME);
			context.addServlet(MatchesByMapServlet.class, MatchesByMapServlet.NAME);
			context.addServlet(RunServlet.class, RunServlet.NAME);
			context.addServlet(LoginServlet.class, LoginServlet.NAME);
			context.addServlet(LogoutServlet.class, LogoutServlet.NAME);
			context.addServlet(AdminServlet.class, AdminServlet.NAME);
			context.addServlet(AdminActionServlet.class, AdminActionServlet.NAME);
			context.addServlet(UploadServlet.class, UploadServlet.NAME);
			context.addServlet(AnalysisServlet.class, AnalysisServlet.NAME);
			context.addServlet(AnalysisContentServlet.class, AnalysisContentServlet.NAME);
			context.addServlet(MyWebSocketServlet.class, MyWebSocketServlet.NAME);

			EnumSet<DispatcherType> en = EnumSet.of(DispatcherType.REQUEST);
			// Set up filters for login blocking and handling file uploads
			context.addFilter(new FilterHolder(new LoginFilter()), "/*", en);
			context.addFilter(new FilterHolder(new MultiPartFilter()), UploadServlet.NAME, en);
			context.setAttribute("javax.servlet.context.tempdir", new File("/tmp"));
			
			ResourceHandler resourceHandler = new FileHandler();
			resourceHandler.setAliases(true);
			resourceHandler.setResourceBase("static");
			
			// Add contexts
			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] {resourceHandler, context, new DefaultHandler()});
			server.setHandler(handlers);
			
			SelectChannelConnector connector = new SelectChannelConnector();
			connector.setPort(httpPort);
			server.addConnector(connector);
			server.start();
			_log.info("WebServer started");
			server.join();
		} catch (Exception e) {
			_log.fatal("Error running webserver", e);
			_log.warn("You need root permissions to run the webserver on ports 80 and 443, or you can specify different ports.  " +
					"Run with -h for more details.");
			System.exit(1);
		}
	}
	
}

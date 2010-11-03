package web;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import common.Config;

public class WebServer implements Runnable {
	private Config config;
	private Logger _log;
	private AbstractServlet[] servlets = {
			new IndexServlet(),
			new ConnectionsServlet(),
			new DeleteServlet(),
			new MatchesServlet(),
			new RunServlet(),
			new FileServlet(),
			new DebugServlet(),
			};

	public WebServer() {
		config = Config.getConfig();
		_log = config.getLogger();
	}

	public void run() {
		try {
			Server server = new Server();
			
			SelectChannelConnector connector0 = new SelectChannelConnector();
	        connector0.setPort(config.http_port);
	        connector0.setMaxIdleTime(30000);
	        connector0.setRequestHeaderSize(8192);
	        
	        // Add the servlets
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");

			context.addServlet(new ServletHolder(new IndexServlet()),"/");
			for (AbstractServlet s: servlets)
				context.addServlet(new ServletHolder(s), "/" + s.name);
			
			// Add the cometd servlet
			ServletHolder sh = new ServletHolder(new CometServlet());
			context.addServlet(sh, "/comet/*");

			// Add the javascript path
			ContextHandler jsContext = new ContextHandler();
			jsContext.setContextPath("/js");
			jsContext.setHandler(new StaticFileHandler("text/javascript", ".+\\.js"));
			
			// Add the css path
			ContextHandler cssContext = new ContextHandler();
			cssContext.setContextPath("/css");
			cssContext.setHandler(new StaticFileHandler("text/css", ".+\\.css"));
			
			// Add the img path
			ContextHandler imgContext = new ContextHandler();
			imgContext.setContextPath("/img");
			imgContext.setHandler(new ImageHandler());
			
			// Add contexts
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[] {jsContext, cssContext, imgContext, context});
			server.setHandler(contexts);
			
			SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
			ssl_connector.setPort(config.https_port);
			ssl_connector.setKeystore(config.keystore);
			ssl_connector.setPassword(config.keystore_pass);
//			ssl_connector.setSslContext(Config.getSSLContext());
			
			server.setConnectors(new Connector[]{ connector0, ssl_connector});
			server.start();
			server.join();
		} catch (Exception e) {
			_log.log(Level.SEVERE, "Error running webserver", e);
		}
	}
	
}

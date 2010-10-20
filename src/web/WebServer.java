package web;

import java.io.File;
import java.io.IOException;
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
			context.addServlet(new ServletHolder(new IndexServlet()),"/" + IndexServlet.name);
			context.addServlet(new ServletHolder(new ConnectionsServlet()),"/" + ConnectionsServlet.name);
			context.addServlet(new ServletHolder(new DeleteServlet()),"/" + DeleteServlet.name);
			context.addServlet(new ServletHolder(new MatchesServlet()),"/" + MatchesServlet.name);
			context.addServlet(new ServletHolder(new RunServlet()),"/" + RunServlet.name);
			context.addServlet(new ServletHolder(new FileServlet()),"/" + FileServlet.name);
			context.addServlet(new ServletHolder(new DebugServlet()),"/" + DebugServlet.name);
			
			// Add the javascript server
			ContextHandler jsContext = new ContextHandler();
			jsContext.setContextPath("/js");
			jsContext.setHandler(new JavascriptHandler());
			
			// Add both contexts
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[] {jsContext, context});
			server.setHandler(contexts);
			
			SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
			ssl_connector.setPort(config.https_port);
			File ks = new File(config.home + "/keystore");
			if (!ks.exists()) {
				makeDefaultKeystore();
			}
			ssl_connector.setKeystore(config.home + "/keystore");
			ssl_connector.setPassword(config.keytool_pass);
			
			server.setConnectors(new Connector[]{ connector0, ssl_connector});
			server.start();
			server.join();
		} catch (Exception e) {
			_log.log(Level.SEVERE, "Error running webserver", e);
		}
	}
	
	private void makeDefaultKeystore() throws IOException, InterruptedException {
		_log.info("Creating keystore");
		Process p = Runtime.getRuntime().exec(new String[] {"./scripts/gen_keystore.sh", 
				config.home + "/keystore", config.keytool_pass});
		p.waitFor();
	}
}

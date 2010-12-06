package web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Config;

/**
 * Redirect traffic on HTTP port to HTTPS port
 * @author stevearc
 *
 */
public class ProxyServlet extends HttpServlet {
	private static final long serialVersionUID = -5863304990385574135L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", "https://" + request.getServerName() + ":" + Config.getConfig().https_port);
	}
}

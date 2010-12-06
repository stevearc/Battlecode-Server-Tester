package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Serve static files
 * @author stevearc
 *
 */
public class StaticFileHandler extends AbstractHandler {
	private String content;
	private String regex;
	
	/**
	 * 
	 * @param content The type of the content served
	 * @param regex All requests matching this regex will be served
	 */
	public StaticFileHandler(String content, String regex) {
		this.content = content;
		this.regex = regex;
	}

	@Override
	public void handle(String path, Request baseRequest, HttpServletRequest servletRequest,
			HttpServletResponse response) throws IOException, ServletException {
		URL url = getClass().getResource(path);
		if (url == null || !path.matches(regex)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			baseRequest.setHandled(true);
			return;
		}
		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.setContentType(content);
		BufferedReader read = new BufferedReader(new InputStreamReader(url.openStream()));
		for (String line = read.readLine(); line != null; line = read.readLine()) {
			out.println(line);
		}
	}

}

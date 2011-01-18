package web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import common.Config;

/**
 * Serve static files
 * @author stevearc
 *
 */
public class StaticFileHandler extends AbstractHandler {
	private String content;
	private String regex;
	private String directory;
	
	/**
	 * 
	 * @param content The type of the content served
	 * @param regex All requests matching this regex will be served
	 */
	public StaticFileHandler(String content, String regex, String directory) {
		this.content = content;
		this.regex = regex;
		this.directory = directory;
	}

	@Override
	public void handle(String path, Request baseRequest, HttpServletRequest servletRequest,
			HttpServletResponse response) throws IOException, ServletException {
		String absPath = directory + path;
		File f = new File(absPath);
		if (!f.exists() || f.isDirectory() || !path.matches(regex)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			baseRequest.setHandled(true);
			return;
		}
		BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(absPath)));
		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.setContentType(content);
		for (String line = read.readLine(); line != null; line = read.readLine()) {
			out.println(line);
		}
	}

}

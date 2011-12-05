package web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

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
		FileInputStream fis = new FileInputStream(absPath);
		
		byte[] buffer = new byte[1000];
		OutputStream ostream = response.getOutputStream();
		int len = 0;
		while ((len = fis.read(buffer)) != -1) {
			ostream.write(buffer, 0, len);
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.setContentType(content);
	}

}

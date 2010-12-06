package web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Serves static image files of type png, jpg, tiff, gif, and bmp
 * @author stevearc
 *
 */
public class ImageHandler extends AbstractHandler {
	private static final String imageRegex = ".+\\.(png|jpg|jpeg|tiff|gif|bmp)";

	@Override
	public void handle(String path, Request baseRequest, HttpServletRequest servletRequest,
			HttpServletResponse response) throws IOException, ServletException {
		URL url = getClass().getResource(path);
		if (url == null || !path.matches(imageRegex)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			baseRequest.setHandled(true);
			return;
		}
		OutputStream out = response.getOutputStream();
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.setContentType("image/" + path.substring(path.lastIndexOf(".")+1).toLowerCase());
		response.setHeader("Content-Type", "image/" + path.substring(path.lastIndexOf(".")+1).toLowerCase());
		InputStream istream = url.openStream();
		
		byte[] buffer = new byte[1024];
		int n;
		while ((n = istream.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}	
	}

}

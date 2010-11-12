package web;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class WebUtil {
	
	public static void writeTabs(HttpServletResponse response, PrintWriter out) {
		// Header with tabs
		out.println("<div id=\"tabs\"><h2>");
		out.println("<ul>" +
				"<li><a href='" + response.encodeURL(IndexServlet.NAME) + "'><span>Home</span></a></li>" +
				"<li><a href='" + response.encodeURL(ConnectionsServlet.NAME) + "'><span>Connections</span></a></li>" +
				"<li><a href='" + response.encodeURL(MapAnalysisServlet.NAME) + "'><span>Map Analysis</span></a></li>" +
				"</ul>");
		out.println("</h2></div>");
		out.println("<p>&nbsp;</p>");
		out.println("<p>&nbsp;</p>");
	}

}

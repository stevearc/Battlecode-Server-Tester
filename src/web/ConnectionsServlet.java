package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.ClientRepr;

import common.Config;
import common.Match;

public class ConnectionsServlet extends AbstractServlet {
	private static final long serialVersionUID = 2147508188812654640L;
	public static final String name = "connections.html";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<meta http-equiv=\"refresh\" content=\"10\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<a href=\"" + response.encodeURL(IndexServlet.name) + "\">back</a><br /><br />");
		out.println("<table border=\"1\">");
		out.println("<tr>" +
				"<th>Client</th>" +
				"<th>Map</th>" +
		"</tr>");
		for (ClientRepr c: Config.getServer().getConnections()) {
			out.println("<tr>");
			out.println("<td>" + c.connectionString() + "</td>");
			out.println("<td>");
			for (Match m: c.getRunningMatches()) {
				out.println(m.map + ", ");
			}
			out.println("</td>");
			out.println("</tr>");
		}
		out.println("</table>" +
				"</body>" +
		"</html>");


	}

}

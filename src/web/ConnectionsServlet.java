package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Timer;

public class ConnectionsServlet extends AbstractServlet {
	private static final long serialVersionUID = 2147508188812654640L;
	public static final String name = "connections.html";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester - " + config.team + "</title>");
		out.println("<meta http-equiv=\"refresh\" content=\"30\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<a href=\"" + response.encodeURL(IndexServlet.name) + "\">back</a><br /><br />");
		out.println("<table border=\"1\">");
		out.println("<tr>" +
				"<th>Client</th>" +
				"<th>Map</th>" +
				"<th>Time</th>" +
		"</tr>");
		try {
			ResultSet rs = db.query("SELECT addr, map, modified, now() as now " +
			"FROM connections c LEFT JOIN running_matches r ON c.id = r.conn_id ORDER BY addr, map");
			while (rs.next()) {
				out.println("<tr>");
				out.println("<td>" + rs.getString("addr") + "</td>");
				out.println("<td>" + (rs.getString("map") == null ? "" : rs.getString("map")) + "</td>");
				Timestamp now = rs.getTimestamp("now");
				Timestamp modified = rs.getTimestamp("modified");
				if (modified != null) {
					long diff = now.getTime() - modified.getTime();
					out.println("<td>" + new Timer(diff) + "</td>");
				} else {
					out.println("<td />");
				}
				out.println("</tr>");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
		out.println("</table>" +
				"</body>" +
		"</html>");


	}

}

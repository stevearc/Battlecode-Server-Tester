package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MatchesServlet extends AbstractServlet {
	private static final long serialVersionUID = 3122992891626513814L;
	public static final String name = "matches.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester" + config.team + "</title>");
		out.println("<script type=\"text/javascript\">");
		// AJAX call to FileServlet
		out.println("function downloadMatch(id, map, run_id) {\n" +
				"document.location = \"" + response.encodeURL(FileServlet.name) + "?id=\" + id + \"" +
						"&map=\" + map + \"&run_id=\" + run_id;\n" + 
		"}");
		out.println("</script>");
		out.println("</head>");
		out.println("<body>");
		out.println("<a href=\"" + response.encodeURL(IndexServlet.name)+ "\">back</a><br />");
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		int id = Integer.parseInt(strId);
		try {
			PreparedStatement st = db.prepare("SELECT id, team_a, t1.alias a_nick, team_b, t2.alias b_nick " +
					"FROM runs r LEFT JOIN tags t1 ON r.team_a = t1.tag " +
			"LEFT JOIN tags t2 ON r.team_b = t2.tag WHERE id = ?");
			st.setInt(1, id);
			ResultSet rs = db.query(st);
			rs.next();
			PreparedStatement st2 = db.prepare("SELECT SUM(win) as wins, COUNT(*) - SUM(win) as losses FROM matches WHERE run_id = ?");
			st2.setInt(1, id);
			ResultSet rs2 = db.query(st2);
			out.println("<table border=\"0\">" +
			"<tr>");
			out.println("<th>Team A</th>");
			out.println("<th>Team B</th>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td>" + rs.getString("team_a") + "</td>");
			out.println("<td>" + rs.getString("team_b") + "</td>");
			out.println("</tr>");
			while (rs2.next()) {
				out.println("<tr>");
				out.println("<td>" + rs2.getInt("wins") + "</td>");
				out.println("<td>" + rs2.getInt("losses") + "</td>");
				out.println("</tr>");
			}
			out.println("</table><br /><br />");
			st2.close();
			st.close();

			out.println("<table border=\"1\">");
			out.println("<tr>");
			out.println("<th>Map</th>");
			out.println("<th>Winner</th>");
			out.println("</tr>");
			PreparedStatement st3 = db.prepare("SELECT * FROM matches WHERE run_id = ? ORDER BY map");
			st3.setInt(1, id);
			ResultSet rs3 = db.query(st3);
			while (rs3.next()) {
				out.println("<tr>");
				out.println("<td>" + rs3.getString("map") + "</td>");
				out.println("<td>" + (rs3.getInt("win") == 1 ? "A" : "B") + "</td>");
				out.println("<td><input type=button value=\"download\" onclick=\"downloadMatch(" + 
						rs3.getString("id") + ", '" + rs3.getString("map") + "', " + rs3.getString("run_id") + ")\"></td>");
				out.println("</tr>");
			}
			st3.close();
			out.println("</table>");
			out.println("</body></html>");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
	}
}

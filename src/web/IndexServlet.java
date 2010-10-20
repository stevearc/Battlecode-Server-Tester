package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Timer;

public class IndexServlet extends AbstractServlet {
	private static final long serialVersionUID = -2587225634870177013L;
	public static final String name = "index.html";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<meta http-equiv=\"refresh\" content=\"30\">");
		out.println("<script type=\"text/javascript\" src=\"js/index.js\"></script>");
		out.println("</head>");
		out.println("<body>");
		String search = request.getParameter("search");
		if (search != null) {
			out.println("<a href=\"" + response.encodeURL(IndexServlet.name) + "\">home</a><br /><br />");
		}
		out.println("Enter the versions to run");

		// Run a new match
		out.println("<form action=\"" + response.encodeURL(RunServlet.name) + "\" method=\"post\">" +
				"<P>" +
				"<label for=\"team_a\">Team A<label>" +
				"<input type=\"text\" name=\"team_a\" id=\"team_a\" size=\"15\"><br />" +
				"<label for=\"team_b\">Team B<label>" +
				"<input type=\"text\" name=\"team_b\" id=\"team_b\" size=\"15\"><br />" +
				"<input type=\"button\" value=\"Run\" onclick=\"newRun(team_a.value, team_b.value)\"><br />" +
				"</P>" +
		"</form>");
		
		// search
		out.println("<form action=\"\">" +
				"Search:<input type=\"text\" name=\"search\" id=\"search\" size=15 method=\"get\" />" +
				"<input type=\"submit\" value=\"Search\" name=\"submit\" />" +
				"</form>");
		
		// TODO: svn alias

		out.println("<a href=\"" + response.encodeURL(ConnectionsServlet.name) + "\">connections</a>");
		out.println("<table border=\"1\">" +
				"<tr>" +
				"<th>Run ID</th>" +
				"<th>Team A</th>" +
				"<th>Team B</th>" +
				"<th>Wins</th>" +
				"<th>Matches</th>" +
				"<th>Status</th>" +
				"<th>Time</th>" +
		"</tr>");

		// Display current runs
		try {
			String sql = "SELECT id, team_a, t1.alias a_nick, team_b, t2.alias b_nick, status, " +
			"started, ended, now() as now " +
			"FROM runs r LEFT JOIN tags t1 ON r.team_a = t1.tag LEFT JOIN tags t2 ON r.team_b = t2.tag";
			if (search != null) {
				sql += " WHERE team_a RLIKE ? OR team_b RLIKE ?";
			}
			sql += " ORDER BY id DESC";
			PreparedStatement st = db.prepare(sql);
			if (search != null) {
				st.setString(1, search);
				st.setString(2, search);
			}
			ResultSet rs = db.query(st);
			while (rs.next()) {
				ResultSet mapsQuery = db.query("SELECT COUNT(*) AS maps, SUM(win) AS wins FROM matches WHERE run_id = " + rs.getInt("id"));
				mapsQuery.next();
				out.println("<tr>" +
						"<td>" + rs.getInt("id") + "</td>" + 
						"<td>" + rs.getString("team_a") + "</td>" +
						"<td>" + rs.getString("team_b") + "</td>" +				
						"<td>" + mapsQuery.getInt("wins") + "/" + mapsQuery.getInt("maps") + "</td>");
				int status = rs.getInt("status");
				switch (status){
				case 0:
					out.println("<td />");
					out.println("<td>Queued</td>");
					out.println("<td />");
					out.println("<td><input type=\"button\" value=\"dequeue\" onclick=\"delRun(" + rs.getInt("id") + ", false)\"></td>");
					break;
				case 1:
					out.println("<td><a href=\"" + response.encodeURL(MatchesServlet.name) + "?id=" + rs.getInt("id") + "\">matches</a></td>");
					out.println("<td>Running</td>");
					Timestamp now = rs.getTimestamp("now");
					Timestamp started = rs.getTimestamp("started");
					long sofar = now.getTime() - started.getTime();
					out.println("<td>" + new Timer(sofar) + "</td>");
					out.println("<td><input type=\"button\" value=\"cancel\" onclick=\"delRun(" + rs.getInt("id") + ", false)\"></td>");
					break;
				case 2:
					out.println("<td><a href=\"" + response.encodeURL(MatchesServlet.name) + "?id=" + rs.getInt("id") + "\">matches</a></td>");
					out.println("<td>Complete</td>");
					Timestamp ended = rs.getTimestamp("ended");
					Timestamp start = rs.getTimestamp("started");
					long taken = ended.getTime() - start.getTime();
					out.println("<td>" + new Timer(taken) + "</td>");
					out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + rs.getInt("id") + ", true)\"></td>");
					break;
				case 3:
					out.println("<td />");
					out.println("<td>Error</td>");
					out.println("<td />");
					out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + rs.getInt("id") + ", false)\"></td>");
					break;
				default:
					out.println("<td />");
					out.println("<td>Unknown Error</td>");
					out.println("<td />");
					out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + rs.getInt("id") + ", false)\"></td>");
				}
				out.println("</tr>");
				mapsQuery.close();
			}
			st.close();
			rs.close();
			out.println("</table></body></html>");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}


	}
}

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
		out.println("<title>Battlecode Tester - " + config.team + "</title>");
		out.println("<meta http-equiv=\"refresh\" content=\"30\">");
		out.println("<script type=\"text/javascript\">");
		// AJAX call to RunServlet
		out.println("function newRun(team_a, team_b) {\n" + 
				"if (team_a.length==0 || team_b.length==0) {\n" + 
				"alert(\"Must have a non-empty team name\");\n" +
				"return;\n" + 
				"}\n" + 
				"xmlhttp=new XMLHttpRequest();\n" + 
				"xmlhttp.onreadystatechange=function() {\n" + 
				"if (xmlhttp.readyState==4 && xmlhttp.status==200) {\n" +
				"if (xmlhttp.responseText == \"err team_a\") {\n" + 
				"alert(\"Must have a valid name for Team A\");\n" +
				"} else if (xmlhttp.responseText == \"err team_b\") {\n" + 
				"alert(\"Must have a valid name for Team B\");\n" + 
				"} else if (xmlhttp.responseText != \"success\") {\n" +
				"alert(xmlhttp.responseText);\n" + 
				"} else {\n" + 
				"location.reload(true);\n" +
				"}\n" + 
				"}\n" + 
				"}\n" + 
				"xmlhttp.open(\"GET\",\"" + response.encodeURL(RunServlet.name) + "?team_a=\"+team_a+\"&team_b=\"+team_b,true);\n" + 
				"xmlhttp.send();\n" + 
		"}");
		// AJAX call to DeleteServlet
		out.println("function delRun(id) {\n" + 
				"if(!confirm(\"This will delete the run and all replay files.  Continue?\")) {\n" +
				"return;\n" + 
				"}\n" + 
				"xmlhttp=new XMLHttpRequest();\n" + 
				"xmlhttp.onreadystatechange=function() {\n" + 
				"if (xmlhttp.readyState==4 && xmlhttp.status==200) {\n" +
				"if (xmlhttp.responseText != \"success\") {\n" +
				"alert(xmlhttp.responseText);\n" + 
				"} else {\n" + 
				"location.reload(true);\n" +
				"}\n" + 
				"}\n" + 
				"}\n" + 
				"xmlhttp.open(\"GET\",\"" + response.encodeURL(DeleteServlet.name) + "?id=\"+id,true);\n" + 
				"xmlhttp.send();\n" + 
		"}");
		// AJAX call to DequeueServlet
		out.println("function dqRun(id) {\n" + 
				"xmlhttp=new XMLHttpRequest();\n" + 
				"xmlhttp.onreadystatechange=function() {\n" + 
				"if (xmlhttp.readyState==4 && xmlhttp.status==200) {\n" +
				"if (xmlhttp.responseText != \"success\") {\n" +
				"alert(xmlhttp.responseText);\n" + 
				"} else {\n" + 
				"location.reload(true);\n" +
				"}\n" + 
				"}\n" + 
				"}\n" + 
				"xmlhttp.open(\"GET\",\"" + response.encodeURL(DequeueServlet.name) + "?id=\"+id,true);\n" + 
				"xmlhttp.send();\n" + 
		"}");
		// AJAX call to CancelServlet
		out.println("function cancelRun(id) {\n" + 
				"xmlhttp=new XMLHttpRequest();\n" + 
				"xmlhttp.onreadystatechange=function() {\n" + 
				"if (xmlhttp.readyState==4 && xmlhttp.status==200) {\n" +
				"if (xmlhttp.responseText != \"success\") {\n" +
				"alert(xmlhttp.responseText);\n" + 
				"} else {\n" + 
				"location.reload(true);\n" +
				"}\n" + 
				"}\n" + 
				"}\n" + 
				"xmlhttp.open(\"GET\",\"" + response.encodeURL(CancelServlet.name) + "?id=\"+id,true);\n" + 
				"xmlhttp.send();\n" + 
		"}");
		out.println("</script>");
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

		// Print queued runs
		try {
			ResultSet rs = db.query("SELECT * FROM queue ORDER BY id DESC");
			while (rs.next()) {
				out.println("<tr>" +
						"<td>?</td>" +
						"<td>" + rs.getString("team_a") + "</td>" +
						"<td>" + rs.getString("team_b") + "</td>" +
						"<td>0</td>" +
						"<td>matches</td>" +
						"<td>queued</td>" +
						"<td />" + 
						"<td><input type=\"button\" value=\"dequeue\" onclick=\"dqRun(" + rs.getInt("id") + ")\"></td>" +
				"</tr>");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace(out);
		}

		// Display current runs
		try {
			String sql = "SELECT id, team_a, t1.alias a_nick, team_b, t2.alias b_nick, finished, " +
			"timediff(ended, started) as taken, timediff(now(), started) as sofar " +
			"FROM runs r LEFT JOIN tags t1 ON r.team_a = t1.tag LEFT JOIN tags t2 ON r.team_b = t2.tag";
			sql = "SELECT id, team_a, t1.alias a_nick, team_b, t2.alias b_nick, finished, " +
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
						"<td>" + mapsQuery.getInt("wins") + "/" + mapsQuery.getInt("maps") + "</td>" +
						"<td><a href=\"" + response.encodeURL(MatchesServlet.name) + "?id=" + rs.getInt("id") + "\">matches</a></td>");
				switch (rs.getInt("finished")){
				case 0:
					out.println("<td>Running </td>");
					break;
				case 1:
					out.println("<td>Complete</td>");
					break;
				default:
					out.println("<td>Error</td>");
				}
				if (rs.getInt("finished") == 1) {
					Timestamp ended = rs.getTimestamp("ended");
					Timestamp started = rs.getTimestamp("started");
					long taken = ended.getTime() - started.getTime();
					out.println("<td>" + new Timer(taken) + "</td>");
				} else if (rs.getInt("finished") == 0) {
					Timestamp now = rs.getTimestamp("now");
					Timestamp started = rs.getTimestamp("started");
					long sofar = now.getTime() - started.getTime();
					out.println("<td>" + new Timer(sofar) + "</td>");
				} else {
					out.println("<td />");
				}
				if (rs.getInt("finished") != 0)
					out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + rs.getInt("id") + ")\"></td>");
				else
					out.println("<td><input type=\"button\" value=\"cancel\" onclick=\"cancelRun(" + rs.getInt("id") + ")\"></td>");
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

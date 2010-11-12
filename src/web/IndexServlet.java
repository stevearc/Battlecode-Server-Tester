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
	public static final String NAME = "index.html";

	public IndexServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"/css/tinytable.css\" />");
		out.println("<link rel=\"stylesheet\" href=\"/css/tabs.css\" />");
		out.println("</head>");
		out.println("<body>");
		
		WebUtil.writeTabs(response, out);
		
		out.println("<div id=\"tablewrapper\">");
		// Run a new match
		out.println("<div class='tabbutton'>");
		out.println("<a onClick='toggleNewRun()' " +
				"style='cursor:pointer;'><span>New Run</span></a>");
		out.println("<p>&nbsp;</p>");
		out.println("<p>&nbsp;</p>");
		out.println("<p>&nbsp;</p>");
		out.println("<form id='add_run' class=\"removed\" action=\"" + response.encodeURL(RunServlet.NAME) + "\" " +
				"style='background:#6CC0FF'>" +
				"<p>" +
				"<label for=\"team_a\">Team A</label>" +
				"<input type=\"text\" name=\"team_a\" id=\"team_a_button\" size=\"15\"><br />" +
				"<label for=\"team_b\">Team B</label>" +
				"<input type=\"text\" name=\"team_b\" id=\"team_b_button\" size=\"15\"><br />" +
				"<input type=\"button\" value=\"Run\" onclick=\"newRun(team_a.value, team_b.value)\"><br />" +
				"</p>" +
		"</form>");
		out.println("</div>");

		

		out.println("<div id=\"tableheader\">" +
				"<div class=\"search\">" +
		"<select id=\"columns\" onchange=\"sorter.search('query')\"></select>");
		out.println("<input type=\"text\" id=\"query\" onkeyup=\"sorter.search('query')\" />");
		out.println("</div>");
		out.println("<span class=\"details\">" +
		"<div>Records <span id=\"startrecord\"></span>-<span id=\"endrecord\"></span> of <span id=\"totalrecords\"></span></div>");
		out.println("<div><a href=\"javascript:sorter.reset()\">reset</a></div>" +
		"</span>");
		out.println("</div>");
		out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"table\" class=\"tinytable\">" +
				"<thead>" + 
				"<tr>" +
				"<th class='desc'><h3>Run ID</h3></th>" +
				"<th class='nosort'><h3>Team A</h3></th>" +
				"<th class='nosort'><h3>Team B</h3></th>" +
				"<th class='nosort'><h3>Wins</h3></th>" +
				"<th class='nosort'><h3>Status</h3></th>" +
				"<th class='nosort'><h3>Time</h3></th>" +
				"<th class='nosort'><h3>Control</h3></th>" +
				"</tr>" +
				"</thead>" +
		"<tbody>");

		// Display current runs
		try {
			String sql = "SELECT id, team_a, t1.alias a_nick, team_b, t2.alias b_nick, status, " +
			"started, ended, now() as now " +
			"FROM runs r LEFT JOIN tags t1 ON r.team_a = t1.tag LEFT JOIN tags t2 ON r.team_b = t2.tag" +
			" ORDER BY id DESC";
			PreparedStatement st = db.prepare(sql);
			ResultSet rs = db.query(st);
			long startTime = 0;
			while (rs.next()) {
				int status = rs.getInt("status");
				ResultSet mapsQuery = db.query("SELECT COUNT(*) AS maps, SUM(win) AS wins FROM matches WHERE run_id = " + rs.getInt("id"));
				mapsQuery.next();
				String td;
				if (status == 1 || status == 2)
					td = "<td onClick='doNavMatches(" + rs.getInt("id") + ")' style='cursor:pointer'>";
				else
					td = "<td>";
				out.println("<tr>");
				out.println(td + rs.getInt("id") + "</td>" + 
						td + rs.getString("team_a") + "</td>" +
						td + rs.getString("team_b") + "</td>" +				
						td + mapsQuery.getInt("wins") + "/" + (mapsQuery.getInt("maps")-mapsQuery.getInt("wins")) + "</td>");
				switch (status){
				case 0:
					out.println("<td>Queued</td>");
					out.println("<td>&nbsp</td>");
					out.println("<td><input type=\"button\" value=\"dequeue\" onclick=\"delRun(" + rs.getInt("id") + ", false)\"></td>");
					break;
				case 1:
					out.println(td + "Running</td>");
					Timestamp now = rs.getTimestamp("now");
					Timestamp started = rs.getTimestamp("started");
					long sofar = now.getTime() - started.getTime();
					startTime = sofar/1000;
					out.println(td + "<a id=\"cntdwn\" name=" + startTime + "></a></td>");
					out.println("<td><input type=\"button\" value=\"cancel\" onclick=\"delRun(" + rs.getInt("id") + ", false)\"></td>");
					break;
				case 2:
					out.println(td + "Complete</td>");
					Timestamp ended = rs.getTimestamp("ended");
					Timestamp start = rs.getTimestamp("started");
					long taken = ended.getTime() - start.getTime();
					out.println(td + new Timer(taken) + "</td>");
					out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + rs.getInt("id") + ", true)\"></td>");
					break;
				case 3:
					out.println("<td>Error</td>");
					out.println("<td>&nbsp</td>");
					out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + rs.getInt("id") + ", false)\"></td>");
					break;
				default:
					out.println("<td>Unknown Error</td>");
					out.println("<td>&nbsp</td>");
					out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + rs.getInt("id") + ", false)\"></td>");
				}
				out.println("</tr>");
				mapsQuery.close();
			}
			st.close();
			rs.close();
			out.println("</tbody>");
			out.println("</table>");
			out.println("<div id=\"tablefooter\">");
			out.println("<div id=\"tablenav\">");
			out.println("<div>");
			out.println("<img src=\"images/first.gif\" width=\"16\" height=\"16\" alt=\"First Page\" onclick=\"sorter.move(-1,true)\" />");
			out.println("<img src=\"images/previous.gif\" width=\"16\" height=\"16\" alt=\"Previous Page\" onclick=\"sorter.move(-1)\" />");
			out.println("<img src=\"images/next.gif\" width=\"16\" height=\"16\" alt=\"Next Page\" onclick=\"sorter.move(1)\" />");
			out.println("<img src=\"images/last.gif\" width=\"16\" height=\"16\" alt=\"Last Page\" onclick=\"sorter.move(1,true)\" />");
			out.println("</div>");
			out.println("<div>");
			out.println("<select id=\"pagedropdown\"></select>");
			out.println("</div>");
			out.println("<div>");
			out.println("<a href=\"javascript:sorter.showall()\">view all</a>");
			out.println("</div>");
			out.println("</div>");
			out.println("<div id=\"tablelocation\">");
			out.println("<div>");
			out.println("<select onchange=\"sorter.size(this.value)\">");
			out.println("<option value=\"5\">5</option>");
			out.println("<option value=\"10\" selected=\"selected\">10</option>");
			out.println("<option value=\"20\">20</option>");
			out.println("<option value=\"50\">50</option>");
			out.println("</select>");
			out.println("<span>Entries Per Page</span>");
			out.println("</div>");
			out.println("<div class=\"page\">Page <span id=\"currentpage\"></span> of <span id=\"totalpages\"></span></div>");
			out.println("</div></div></div>");

			out.println("<script type=\"text/javascript\" src=\"js/countdown.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/async.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/index.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/init_table.js\"></script>");
			out.println("</body></html>");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}


	}
}

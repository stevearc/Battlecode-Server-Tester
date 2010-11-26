package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.BattlecodeMap;
import common.Config;
import common.Timer;

public class IndexServlet extends AbstractServlet {
	private static final long serialVersionUID = -2587225634870177013L;
	public static final String NAME = "index.html";

	public IndexServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null) {
			redirect(response);
			return;
		}
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

		try {
			ArrayList<String> tags = new ArrayList<String>();
			ResultSet tagSet = db.query("SELECT * FROM tags");
			while (tagSet.next()) {
				String team = tagSet.getString("alias");
				team = (team == null ? tagSet.getString("tag") : team);
				tags.add(team);
			}
			String[] sorted_tags = tags.toArray(new String[tags.size()]);
			Arrays.sort(sorted_tags);
			out.println("<div id=\"tablewrapper\">");
			// Begin new run form
			String background = "#CEFFFC";
			out.println("<div class='tabbutton'>");
			out.println("<a onClick='toggleNewRun()' " +
			"style='cursor:pointer;'><span>New Run</span></a>");
			out.println("<p>&nbsp;</p>");
			out.println("<p>&nbsp;</p>");
			out.println("<p style='background:" + background + "'>&nbsp;</p>");
			out.println("<form id='add_run' class=\"removed\" action=\"" + response.encodeURL(RunServlet.NAME) + "\" " +
					"style='background:" + background + "'>");
			out.println("<select id='team_a_button'>");
			for (String t: sorted_tags) {
				out.println("<option name='" + t + "'>" + t + "</option>");
			}
			out.println("</select> vs. ");
			out.println("<select id='team_b_button'>");
			for (String t: sorted_tags) {
				out.println("<option name='" + t + "'>" + t + "</option>");
			}
			out.println("</select>&nbsp;&nbsp;&nbsp;<input type='button' onclick='doRepoUpdate()' value='update'></p>");
			out.println("Matches per map:" +
			"<select id='seed_selector' onChange='numSeedsChange()'>");
			for (int i = 1; i < 11; i++) 
				out.println("<option name='" + i + "'>" + i + "</option>");
			out.println("</select>");
			out.println("<div id='seeds'>");
			out.println("<p id='seed1' class=''>Seed 1: <input id='seed_txt1' type='text' size='15' value='1'></p>");
			for (int i = 2; i < 11; i++)
				out.println("<p id='seed" + i + "' class='removed'>Seed " + i + ": <input id='seed_txt" + i + 
						"' type='text' size='15' value='" + i + "'></p>");
			out.println("</div>");
			out.println("<input type=\"button\" value=\"Start\" onclick=\"newRun();toggleNewRun()\"><br /></p>");

			// Table of maps
			out.println("<table style='width:50%;margin:0 auto' cellpadding='0' cellspacing='0' border='0' id='map_table' class='tinytable'>");
			out.println("<thead>");
			out.println("<tr><th class='nosort' style='text-align:center'><input id='maps_checkbox' " +
					"onClick='toggleMapsCheckbox()' type='checkbox'></th>" +
					"<th class='desc'><h3>Map</h3></th>" +
			"<th class='desc'><h3>Size</h3></th></tr>");
			out.println("</thead><tbody>");
			for (BattlecodeMap m: Config.getServer().getMaps())
				out.println("<tr><td><input type='checkbox' name='" + m.map + "'></td><td>" + m.map + "</td><td>" + m.getSize() + "</td></tr>");
			out.println("</tbody></table>");
			out.println("<input type=\"button\" value=\"Start\" onclick=\"newRun();toggleNewRun()\"><br /></p>");

			out.println("<p style='background:" + background + "'>&nbsp;</p>" +
			"</form>");
			out.println("</div>");
			// End new run form

			WebUtil.printTableHeader(out, "sorter");
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"table\" class=\"tinytable\">" +
					"<thead>" + 
					"<tr>" +
					"<th class='desc'><h3>Run ID</h3></th>" +
					"<th class='desc'><h3>Team A</h3></th>" +
					"<th class='desc'><h3>Team B</h3></th>" +
					"<th class='desc'><h3>Wins</h3></th>" +
					"<th class='desc'><h3>Status</h3></th>" +
					"<th class='desc'><h3>Time</h3></th>" +
					"<th class='nosort'><h3>Control</h3></th>" +
					"</tr>" +
					"</thead>" +
			"<tbody>");

			// Display current runs
			String sql = "SELECT id, team_a, t1.alias a_nick, team_b, t2.alias b_nick, status, " +
			"started, ended, now() as now " +
			"FROM runs r LEFT JOIN tags t1 ON r.team_a = t1.tag LEFT JOIN tags t2 ON r.team_b = t2.tag" +
			" ORDER BY id DESC";
			PreparedStatement st = db.prepare(sql);
			ResultSet rs = db.query(st);
			long startTime = 0;
			while (rs.next()) {
				int status = rs.getInt("status");
				String team_a = rs.getString("a_nick");
				team_a = (team_a == null ? rs.getString("team_a") : "<a title='" + rs.getString("team_a") + "'>" + team_a + "</a>");
				String team_b = rs.getString("b_nick");
				team_b = (team_b == null ? rs.getString("team_b") : "<a title='" + rs.getString("team_b") + "'>" + team_b + "</a>");
				ResultSet mapsQuery = db.query("SELECT COUNT(*) AS maps, SUM(win) AS wins FROM matches WHERE run_id = " + 
						rs.getInt("id") + " AND win IS NOT NULL");
				mapsQuery.next();
				String td;
				if (status == 1 || status == 2)
					td = "<td onClick='doNavMatches(" + rs.getInt("id") + ")' style='cursor:pointer'>";
				else
					td = "<td>";
				out.println("<tr>");
				out.println(td + rs.getInt("id") + "</td>" + 
						td + team_a + "</td>" +
						td + team_b + "</td>" +				
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
			WebUtil.printTableFooter(out, "sorter");
			out.println("</div>");

			ResultSet r = db.query("SELECT COUNT(*) AS total FROM matches m JOIN runs r ON m.run_id = r.id WHERE r.status = 1");
			r.next();
			out.println("<script type=\"text/javascript\">");
			out.println("var total_num_matches = " + r.getInt("total"));
			out.println("</script>");
			r.close();
			r = db.query("SELECT COUNT(*) AS current FROM matches m JOIN runs r ON m.run_id = r.id WHERE r.status = 1 AND m.win IS NOT NULL");
			r.next();
			out.println("<script type=\"text/javascript\">");
			out.println("var current_num_matches = " + r.getInt("current"));
			out.println("</script>");
			r.close();
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

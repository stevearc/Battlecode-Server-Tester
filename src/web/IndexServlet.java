package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSMap;
import model.BSMatch;
import model.BSPlayer;
import model.BSRun;
import model.TEAM;

import common.HibernateUtil;

/**
 * Displays the list of all runs
 * @author stevearc
 *
 */
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = -2587225634870177013L;
	public static final String NAME = "index.html";

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

		WebUtil.writeTabs(response, out, toString());

		out.println("<div id=\"tablewrapper\">");

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
		EntityManager em = HibernateUtil.getEntityManager();
		List<BSRun> runs = em.createQuery("from BSRun", BSRun.class).getResultList();
		for (BSRun r: runs) {
			String td;
			// Make runs with data clickable
			if (r.getStatus() == BSRun.STATUS.RUNNING || r.getStatus() == BSRun.STATUS.COMPLETE || r.getStatus() == BSRun.STATUS.CANCELED)
				td = "<td onClick='doNavMatches(" + r.getId() + ")' style='cursor:pointer'>";
			else
				td = "<td>";
			out.println("<tr>");
			
			// TODO: cache this data in BSRun
			List<Object[]> wins = em.createQuery("select match.result.winner, count(*) from BSMatch match where match.run = ? and match.status = ? group by match.result.winner", Object[].class)
			.setParameter(1, r)
			.setParameter(2, BSMatch.STATUS.FINISHED)
			.getResultList();
			long aWins = 0;
			long bWins = 0;
			for (Object[] valuePair: wins) {
				if (valuePair[0] == TEAM.A) {
					aWins = (Long) valuePair[1];
				} else if (valuePair[0] == TEAM.B) {
					bWins = (Long) valuePair[1];
				}
			}
			
			out.println(td + r.getId() + "</td>" + 
					td + r.getTeamA().getPlayerName() + "</td>" +
					td + r.getTeamB().getPlayerName() + "</td>" +				
					td + aWins + "/" + bWins + "</td>");
			switch (r.getStatus()){
			case QUEUED:
				out.println("<td>Queued</td>");
				out.println("<td>&nbsp</td>");
				out.println("<td><input type=\"button\" value=\"dequeue\" onclick=\"delRun(" + r.getId() + ", false)\"></td>");
				break;
			case RUNNING:
				out.println(td + "Running</td>");
				out.println(td + "<a id=\"cntdwn\" name=" + r.calculateTimeTaken()/1000 + "></a></td>");
				out.println("<td><input type=\"button\" value=\"cancel\" onclick=\"delRun(" + r.getId() + ", false)\"></td>");
				break;
			case COMPLETE:
				out.println(td + "Complete</td>");
				out.println(td + r.printTimeTaken() + "</td>");
				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + r.getId() + ", true)\"></td>");
				break;
			case ERROR:
				out.println("<td>Error</td>");
				out.println("<td>&nbsp</td>");
				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + r.getId() + ", false)\"></td>");
				break;
			case CANCELED:
				out.println(td + "Canceled</td>");
				out.println(td + r.printTimeTaken() + "</td>");
				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + r.getId() + ", true)\"></td>");
				break;
			default: // Run finished with wtf errors
				out.println("<td>Unknown Error</td>");
				out.println("<td>&nbsp</td>");
				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + r.getId() + ", false)\"></td>");
			}
			out.println("</tr>");
		}
		out.println("</tbody>");
		out.println("</table>");
		WebUtil.printTableFooter(out, "sorter");

		// Begin the New Run form
		List<BSPlayer> players = em.createQuery("from BSPlayer player order by player.playerName desc", BSPlayer.class).getResultList();
		out.println("<br /><br />");
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
		for (BSPlayer p: players) {
			out.println("<option value='" + p.getId() + "'>" + p.getPlayerName() + "</option>");
		}
		out.println("</select> vs. ");
		out.println("<select id='team_b_button'>");
		for (BSPlayer p: players) {
			out.println("<option value='" + p.getId() + "'>" + p.getPlayerName() + "</option>");
		}
		out.println("</select></p>");
		out.println("Matches per map: " +
		"<select id='seed_selector' onChange='numSeedsChange()'>");
		for (int i = 1; i < 11; i++) 
			out.println("<option name='" + i + "'>" + i + "</option>");
		out.println("</select>");
		out.println("<div id='seeds'>");
		out.println("<p id='seed1' class=''>Map Seed 1: " +
		"<input id='seed_txt1' type='text' size='8' value='1'></p>");
		for (int i = 2; i < 11; i++)
			out.println("<p id='seed" + i + "' class='removed'>" +
					"Map Seed " + i + ": <input id='seed_txt" + i + 
					"' type='text' size='8' value='" + i + "'></p>");
		out.println("</div>");
		out.println("<input type=\"button\" value=\"Start\" onclick=\"if (newRun()) {toggleNewRun()}\"><br /></p>");
		for (String s: new String[] {"small", "medium", "large"}) {
			out.println(s + ": <input id='maps_checkbox_" + s + "' onClick='toggleMaps(\"" + s + "\")' " +
			"type='checkbox'>");
		}

		// Table of maps
		out.println("<table style='width:50%;margin:0 auto;' cellpadding='0' cellspacing='0' border='0' id='map_table' class='tinytable'>");
		out.println("<thead>");
		out.println("<tr><th class='nosort' style='text-align:center'><input id='maps_checkbox' " +
				"onClick='toggleAllMaps()' type='checkbox'></th>" +
				"<th class='desc'><h3>Map</h3></th>" +
		"<th class='desc'><h3>Size</h3></th></tr>");
		out.println("</thead><tbody>");
		List<BSMap> maps = em.createQuery("from BSMap", BSMap.class).getResultList();
		for (BSMap m: maps) {
			out.println("<tr><td><input type='checkbox' value='" + m.getId() + "'></td>" +
					"<td>" + m.getMapName() + "</td><td>" + m.getSize() + "</td></tr>");
		}
		out.println("</tbody></table>");
		out.println("<input type=\"button\" value=\"Start\" onclick=\"if (newRun()) {toggleNewRun()}\"><br /></p>");

		out.println("<p style='background:" + background + "'>&nbsp;</p>" +
		"</form>");
		out.println("</div>");
		// End new run form
		out.println("</div>");

		List<Object[]> resultList = em.createQuery("select match.status, count(*) from BSMatch match where match.run.status = ? group by match.status", Object[].class)
		.setParameter(1, BSRun.STATUS.RUNNING)
		.getResultList();
		
		long currentMatches = 0;
		long totalMatches = 0;
		for (Object[] valuePair: resultList) {
			if (valuePair[0] == BSMatch.STATUS.FINISHED) {
				currentMatches += (Long) valuePair[1];
			}
			totalMatches += (Long) valuePair[1];
		}
		
		out.println("<script type=\"text/javascript\">");
		out.println("var total_num_matches = " + totalMatches);
		out.println("</script>");
		out.println("<script type=\"text/javascript\">");
		out.println("var current_num_matches = " + currentMatches);
		out.println("</script>");
		out.println("<script type=\"text/javascript\" src=\"js/countdown.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/async.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/index.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/init_table.js\"></script>");
		out.println("</body></html>");
		em.close();
	}
	
	@Override
	public String toString() {
		return NAME;
	}
}

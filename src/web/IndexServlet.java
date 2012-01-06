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
import model.STATUS;

import common.HibernateUtil;
import common.BSUtil;

/**
 * Displays the list of all runs
 * @author stevearc
 *
 */
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = -2587225634870177013L;
	public static final String NAME = "/index.html";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Send them to the upload servlet if they haven't uploaded battlecode files yet
		if (!BSUtil.initializedBattlecode()) {
			response.sendRedirect(UploadServlet.NAME);
			return;
		}
		if (!request.getRequestURI().equals("/") && !request.getRequestURI().equals(NAME)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"/css/table.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(request, response, NAME);
		out.println("<script src='js/jquery.dataTables.min.js'></script>");
		
		EntityManager em = HibernateUtil.getEntityManager();
		// Begin the New Run form
		List<BSPlayer> players = em.createQuery("from BSPlayer player order by player.playerName desc", BSPlayer.class).getResultList();
		out.println("<button id='newRunButton' style='margin-left: 20px'>New Run</button>");
		out.println("<div id='newRunForm' style='display:none'>");
		out.println("<div class='overlay'>");
		out.println("</div>");
		out.println("<div class='overlay-contents'>");
		out.println("<form action='" + response.encodeURL(RunServlet.NAME) + "'>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<td>");
		out.println("<p><span id='startButton'>Start</span></p>");
		out.println("<p>");
		out.println("<select id='team_a_button'>");
		for (BSPlayer p: players) {
			out.println("<option value='" + p.getId() + "'>" + p.getPlayerName() + "</option>");
		}
		out.println("</select> vs. ");
		out.println("<select id='team_b_button'>");
		for (BSPlayer p: players) {
			out.println("<option value='" + p.getId() + "'>" + p.getPlayerName() + "</option>");
		}
		out.println("</select>");
		out.println("</p>");
		out.println("Matches per map: " +
		"<select id='seed_selector' onChange='numSeedsChange()'>");
		for (int i = 1; i < 21; i++) 
			out.println("<option name='" + i + "'>" + i + "</option>");
		out.println("</select>");
		out.println("<div id='seeds' style='font-size:13px'>");
		for (int i = 1; i < 21; i++)
			out.println("<p id='seed" + i + "' class='ui-helper-hidden' style='margin:0'>" +
					"Map Seed " + i + ": <input id='seed_txt" + i + 
					"' type='text' size='8' value='" + i + "'></p>");
		out.println("</div>");
		out.println("</td>");

		// Table of maps
		out.println("<td rowspan='2'>");
		out.println("<table id='map_table' class='datatable'>");
		out.println("<thead>");
		out.println("<tr><th><input id='maps_checkbox' " +
				"type='checkbox'></th>" +
				"<th>Map</th>" +
		"</tr>");
		out.println("</thead><tbody>");
		List<BSMap> maps = em.createQuery("from BSMap", BSMap.class).getResultList();
		for (BSMap m: maps) {
			out.println("<tr><td><input type='checkbox' value='" + m.getId() + "'></td>" +
					"<td>" + m.getMapName() + "</td></tr>");
		}
		out.println("</tbody></table>");
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr><td></td><td></td></tr>");
		out.println("</table>");

		out.println("</form>");
		out.println("</div>");
		out.println("</div>");
		// End new run form

		out.println("<table id=\"run_table\" class='datatable datatable-clickable'>" +
				"<thead>" + 
				"<tr>" +
				"<th>Run ID</th>" +
				"<th>Team A</th>" +
				"<th>Team B</th>" +
				"<th>Wins</th>" +
				"<th>Status</th>" +
				"<th>Time</th>" +
				"<th>Control</th>" +
				"</tr>" +
				"</thead>" +
		"<tbody>");

		// Display current runs
		List<BSRun> runs = em.createQuery("from BSRun", BSRun.class).getResultList();
		for (BSRun r: runs) {
			String td;
			// Make runs with data clickable
			//TODO: move this into js
			if (r.getStatus() == STATUS.RUNNING || r.getStatus() == STATUS.COMPLETE || r.getStatus() == STATUS.CANCELED)
				td = "<td onClick='doNavMatches(" + r.getId() + ")'>";
			else
				td = "<td>";
			out.println("<tr>");
			
			out.println(td + r.getId() + "</td>" + 
					td + r.getTeamA().getPlayerName() + "</td>" +
					td + r.getTeamB().getPlayerName() + "</td>" +				
					td + r.getaWins() + "/" + r.getbWins() + "</td>");
			switch (r.getStatus()){
			case QUEUED:
				out.println("<td>" + r.getStatus() + "</td>");
				out.println("<td>&nbsp</td>");
				out.println("<td><input type=\"button\" value=\"dequeue\" onclick=\"delRun(" + r.getId() + ", false)\"></td>");
				break;
			case RUNNING:
				long currentMatches = 0;
				long totalMatches = 0;
				List<Object[]> resultList = em.createQuery("select match.status, count(*) from BSMatch match " +
						"where match.run = ? group by match.status", Object[].class)
				.setParameter(1, r)
				.getResultList();
				for (Object[] valuePair: resultList) {
					if (valuePair[0] == BSMatch.STATUS.FINISHED) {
						currentMatches += (Long) valuePair[1];
					}
					totalMatches += (Long) valuePair[1];
				}
				out.println(td + (currentMatches*100/totalMatches) + "%</td>");
				out.println(td + "<a id=\"cntdwn\" name=" + r.calculateTimeTaken()/1000 + "></a></td>");
				out.println("<td><input type=\"button\" value=\"cancel\" onclick=\"delRun(" + r.getId() + ", false)\"></td>");
				break;
			case COMPLETE:
				out.println(td + r.getStatus() + "</td>");
				out.println(td + "<span style='display:none'>" + r.calculateTimeTaken()/10000000. + "</span>" + r.printTimeTaken() + "</td>");
				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + r.getId() + ", true)\"></td>");
				break;
			case ERROR:
				out.println("<td>" + r.getStatus() + "</td>");
				out.println("<td>&nbsp</td>");
				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + r.getId() + ", false)\"></td>");
				break;
			case CANCELED:
				out.println(td + r.getStatus() + "</td>");
				out.println(td + r.printTimeTaken() + "</td>");
				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + r.getId() + ", true)\"></td>");
				break;
			default:
				out.println("<td>Unknown Error</td>");
				out.println("<td>&nbsp</td>");
				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delRun(" + r.getId() + ", false)\"></td>");
			}
			out.println("</tr>");
		}
		out.println("</tbody>");
		out.println("</table>");
		out.println("</div>");
		
		out.println("<script type=\"text/javascript\" src=\"js/countdown.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/index.js\"></script>");
		out.println("</body></html>");
		em.close();
	}
	
}

package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSMatch;
import model.BSRun;
import model.BSUser;
import dataAccess.HibernateUtil;




/**
 * View all the matches from a single run
 * @author stevearc
 *
 */
public class MatchesServlet extends AbstractServlet {
	private static final long serialVersionUID = 3122992891626513814L;
	public static final String NAME = "matches.html";

	public MatchesServlet() {
		super(NAME);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BSUser user = checkLogin(request, response);
		if (user == null) {
			redirect(response);
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/tinytable.css\" />");
		out.println("<link rel=\"stylesheet\" href=\"css/tabs.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out, name);

		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		long id = new Long(Integer.parseInt(strId));
		out.println("<div id=\"tablewrapper\">");
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun run = em.find(BSRun.class, id);
		out.println("<h2><font color='red'>" + run.getTeamA().getPlayerName() + "</font> vs. <font color='blue'>" + run.getTeamB().getPlayerName() + "</font></h2>");
		// TODO: this line
		//out.println("<h3>" + WebUtil.getFormattedMapResults(WebUtil.getMapResults(id, null, false)) + "</h3>");
		out.println("<br />");
		out.println("<div class='tabbutton'>");
		out.println("<a onClick='document.location=\"" + response.encodeURL(MatchesByMapServlet.NAME) + "?id=" + id + "\"' " +
		"style='cursor:pointer;'><span>View by map</span></a>");
		out.println("<p>&nbsp;</p>");
		out.println("<p>&nbsp;</p>");
		out.println("<p>&nbsp;</p>");
		out.println("</div>");

		WebUtil.printTableHeader(out, "matches_sorter");
		out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"matches_table\" class=\"tinytable\">" +
				"<thead>" + 
				"<tr>" +
				"<th class='desc'><h3>Map</h3></th>" +
				"<th class='desc'><h3>Map seed</h3></th>" +
				"<th class='desc'><h3>Winner</h3></th>" +
				"<th class='desc'><h3>Size</h3></th>" +
				"<th class='desc'><h3>Win condition</h3></th>" +
				"<th class='desc'><h3>Points</h3></th>" +
				"<th class='nosort'><h3>&nbsp;</h3></th>" +
				"</tr>" +
				"</thead>" +
		"<tbody>");

		List<BSMatch> matches = em.createQuery("from BSMatch match where match.run = ? and match.status = ? order by match.map.mapName", BSMatch.class)
		.setParameter(1, run)
		.setParameter(2, BSMatch.STATUS.FINISHED)
		.getResultList();
		for (BSMatch match: matches) {
			out.println("<tr>");
			out.println("<td>" + match.getMap().getMapName() + "</td>");
			out.println("<td>" + match.getSeed() + "</td>");
			out.println("<td><font color='" + (match.getWinner() == BSMatch.TEAM.TEAM_A ? "red'>" + 
					run.getTeamA().getPlayerName() : "blue'>" + run.getTeamB().getPlayerName()) + "</font></td>");
			out.println("<td>" + match.getMap().calculateSizeClass() + "</td>");
			out.println("<td>" + match.getWinCondition() + "</td>");
			out.println("<td><font color='red'>" + match.getaPoints() + "</font>/<font color='blue'>" + 
					match.getbPoints() + "</font></td>");
			out.println("<td><input type=button value=\"download\" onclick=\"document.location='/matches/" + 
					strId + match.getMap().getMapName() + match.getSeed() + ".rms'\"></td>");
			out.println("</tr>");
		}
		em.close();
		out.println("</tbody>");
		out.println("</table>");
		WebUtil.printTableFooter(out, "matches_sorter");

		out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/matches_init_table.js\"></script>");
		out.println("</body></html>");
	}

}

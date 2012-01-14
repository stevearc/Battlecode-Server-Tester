package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSMatch;
import model.BSRun;
import model.STATUS;
import model.TEAM;

import common.HibernateUtil;

/**
 * View all the matches from a single run.  Should be viewed in an iframe
 * @author stevearc
 *
 */
public class MatchesServlet extends HttpServlet {
	private static final long serialVersionUID = 3122992891626513814L;
	public static final String NAME = "/matches_individual.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("<div class='ui-state-error'>Could not load match table</div>");
			return;
		}
		long id = new Long(Integer.parseInt(strId));
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun run = em.find(BSRun.class, id);

		out.println("<table id='matches_table' class='datatable datatable-clickable'>" +
				"<thead>" + 
				"<tr>" +
				"<th>Map</th>" +
				"<th>Map seed</th>" +
				"<th>Winner</th>" +
				"<th>Size</th>" +
				"<th>Rounds</th>" +
				"<th>Win condition</th>" +
				"<th>Replay File</th>" +
				"<th>Match Output</th>" +
				"</tr>" +
				"</thead>" +
		"<tbody>");

		List<BSMatch> matches = em.createQuery("from BSMatch match where match.run = ? and match.status = ? order by match.map.mapName", BSMatch.class)
		.setParameter(1, run)
		.setParameter(2, STATUS.COMPLETE)
		.getResultList();
		for (BSMatch match: matches) {
			out.println("<tr>");
			out.println(td(match) + match.getMap().getMapName() + "</td>");
			out.println(td(match) + match.getSeed() + "</td>");
			out.println(td(match) + "<font color='" + (match.getResult().getWinner() == TEAM.A ? "red'>" + 
					run.getTeamA().getPlayerName() : "blue'>" + run.getTeamB().getPlayerName()) + "</font></td>");
			out.println(td(match) + match.getMap().getSize() + "</td>");
			out.println(td(match) + match.getResult().getRounds() + "</td>");
			out.println(td(match) + match.getResult().getWinCondition() + "</td>");
			out.println("<td><input type=button value='download' onclick=\"downloadFile('/matches/" + match.toMatchFileName() + "')\"></td>");
			out.println("<td><input type=button value='download' onclick=\"downloadFile('/matches/" + match.toOutputFileName() + "')\"></td>");
			out.println("</tr>");
		}
		em.close();
		out.println("</tbody>");
		out.println("</table>");

		out.println("<script type=\"text/javascript\" src=\"js/matches_individual.js\"></script>");
	}
	
	public String td(BSMatch match) {
		return "<td onclick='rowClick(" + match.getId() + ")'>";
	}

}

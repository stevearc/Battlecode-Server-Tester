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
import model.TEAM;

import common.HibernateUtil;




/**
 * View all the matches from a single run
 * @author stevearc
 *
 */
public class MatchesServlet extends HttpServlet {
	private static final long serialVersionUID = 3122992891626513814L;
	public static final String NAME = "matches.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/table.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out, toString());
		out.println("<script src='js/jquery.dataTables.min.js'></script>");

		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		long id = new Long(Integer.parseInt(strId));
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun run = em.find(BSRun.class, id);
		out.println("<h2><font color='red'>" + run.getTeamA().getPlayerName() + "</font> vs. <font color='blue'>" + run.getTeamB().getPlayerName() + "</font></h2>");
		out.println("<h3>Wins by map: " + WebUtil.getFormattedMapResults(WebUtil.getMapResults(run, null, false)) + "</h3>");
		out.println("<br />");
		out.println("<div id='viewStyle' style='margin-left:20px'>" +
				"<input type='radio' id='byMatch' name='byMatch' checked='checked' /><label for='byMatch'>By Match</label>" +
				"<input type='radio' id='byMap' name='byMap' /><label for='byMap'>By Map</label>" +
				" </div>");
		out.println("<script type='text/javascript'>" +
				"$(function() {" +
				"$('#byMap').click(function() {" +
				"document.location = '" + response.encodeURL(MatchesByMapServlet.NAME) + "?id=" + id + "';" + 
				"});" +
				"});" + 
				"</script>");

		out.println("<table id=\"matches_table\" class='datatable datatable-clickable'>" +
				"<thead>" + 
				"<tr>" +
				"<th>Map</th>" +
				"<th>Map seed</th>" +
				"<th>Winner</th>" +
				"<th>Size</th>" +
				"<th>Win condition</th>" +
				"<th>&nbsp;</th>" +
				"</tr>" +
				"</thead>" +
		"<tbody>");

		List<BSMatch> matches = em.createQuery("from BSMatch match where match.run = ? and match.status = ? order by match.map.mapName", BSMatch.class)
		.setParameter(1, run)
		.setParameter(2, BSMatch.STATUS.FINISHED)
		.getResultList();
		for (BSMatch match: matches) {
			out.println("<tr>");
			out.println(td(match) + match.getMap().getMapName() + "</td>");
			out.println(td(match) + match.getSeed() + "</td>");
			out.println(td(match) + "<font color='" + (match.getResult().getWinner() == TEAM.A ? "red'>" + 
					run.getTeamA().getPlayerName() : "blue'>" + run.getTeamB().getPlayerName()) + "</font></td>");
			out.println(td(match) + match.getMap().getSize() + "</td>");
			out.println(td(match) + match.getResult().getWinCondition() + "</td>");
			out.println("<td><input type=button value=\"download\" onclick=\"document.location='/matches/" + 
					match.toMatchFileName() + "'\"></td>");
			out.println("</tr>");
		}
		em.close();
		out.println("</tbody>");
		out.println("</table>");

		out.println("<script type=\"text/javascript\" src=\"js/matches.js\"></script>");
		out.println("</body></html>");
	}
	
	public String td(BSMatch match) {
		return "<td onclick='rowClick(" + match.getId() + ")'>";
	}

	@Override
	public String toString() {
		return NAME;
	}
}

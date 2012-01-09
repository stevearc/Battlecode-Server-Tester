package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSScrimmageSet;
import model.ScrimmageMatchResult;
import model.TEAM;

import common.HibernateUtil;

public class ScrimmageViewServlet extends HttpServlet{
	private static final long serialVersionUID = -3751070384739237866L;
	public static final String NAME = "/scrim.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/table.css\" />");
		out.println("</head>");
		
		WebUtil.writeTabs(request, response, NAME);
		
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		long id = new Long(Integer.parseInt(strId));
		EntityManager em = HibernateUtil.getEntityManager();
		BSScrimmageSet scrim = em.find(BSScrimmageSet.class, id);
		out.println("<div style='text-align:center'>");
		out.println("<h1>" + scrim.getFileName() + "</h1>");
		out.println("<h2><font color='red'>" + scrim.getPlayerA() + 
				"</font> vs. <font color='blue'>" + scrim.getPlayerB() + "</font></h2>");
		
		out.println("<input style='margin-bottom: 20px' type=button value=\"download\" onclick=\"document.location='/scrimmages/" + 
				scrim.getFileName() + "'\">");
		out.println("</div>");

		out.println("<table id='scrimmage_table' class='datatable datatable-clickable'>" +
				"<thead>" + 
				"<tr>" +
				"<th>Round ID</th>" +
				"<th>Map</th>" +
				"<th>Winner</th>" +
				"<th>Rounds</th>" +
				"<th>Win condition</th>" +
				"</tr>" +
				"</thead>" +
		"<tbody>");

		List<ScrimmageMatchResult> rounds = new ArrayList<ScrimmageMatchResult>();
		rounds.add(scrim.getScrim1());
		if (scrim.getScrim2() != null)
			rounds.add(scrim.getScrim2());
		if (scrim.getScrim3() != null)
			rounds.add(scrim.getScrim3());
		for (ScrimmageMatchResult result: rounds) {
			out.println("<tr>");
			out.println(td(result) + result.getId() + "</td>");
			out.println(td(result) + result.getMap() + "</td>");
			out.println(td(result) + "<font color='" + (result.getWinner() == TEAM.A ? "red'>" + scrim.getPlayerA() : "blue'>" + scrim.getPlayerB()) + "</font></td>");
			out.println(td(result) + result.getRounds() + "</td>");
			out.println(td(result) + result.getWinCondition() + "</td>");
			out.println("</tr>");
		}
		em.close();
		out.println("</tbody>");
		out.println("</table>");

		out.println("<script src='js/jquery.dataTables.min.js'></script>");
		out.println("<script src=\"js/single_scrim.js\"></script>");
		out.println("</body></html>");
	}
	
	public String td(ScrimmageMatchResult result) {
		return "<td onclick='rowClick(" + result.getId() + ")'>";
	}
}

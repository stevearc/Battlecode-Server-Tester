package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSMatch;

import common.HibernateUtil;

public class AnalysisServlet extends HttpServlet {
	private static final long serialVersionUID = 8020173508294249410L;
	public static final String NAME = "analysis.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/jquery.jqplot.min.css\" />");
		out.println("</head>");
		out.println("<body>");
		
		WebUtil.writeTabs(request, response, out, toString());
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		long id = new Long(Integer.parseInt(strId));
		EntityManager em = HibernateUtil.getEntityManager();
		BSMatch match = em.find(BSMatch.class, id);
		out.println("<button id='back' style='margin-left:10px; margin-right:-70px; float:left' name='" + match.getRun().getId() + "'>Back</button>");
		AnalysisContentServlet.printContent(request, response, match);
	}
	
	@Override
	public String toString() {
		return NAME;
	}
}

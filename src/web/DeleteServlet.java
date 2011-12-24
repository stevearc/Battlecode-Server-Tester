package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.AbstractMaster;


/**
 * Delete the results of a run
 * @author stevearc
 *
 */
public class DeleteServlet extends HttpServlet {
	private static final long serialVersionUID = 4149519483270976451L;
	public static final String NAME = "/delete.html";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id: " + strId);
		}

		AbstractMaster.kickoffDeleteRun(new Long(Integer.parseInt(strId)));
		out.print("success");
	}
	
}

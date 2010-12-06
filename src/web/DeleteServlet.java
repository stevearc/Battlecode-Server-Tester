package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.ServerMethodCaller;

/**
 * Delete the results of a run
 * @author stevearc
 *
 */
public class DeleteServlet extends AbstractServlet {
	private static final long serialVersionUID = 4149519483270976451L;
	public static final String NAME = "delete.html";
	
	public DeleteServlet() {
		super(NAME);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null) {
			redirect(response);
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id: " + strId);
		}

		int id = Integer.parseInt(strId);
		ServerMethodCaller.deleteRun(id);
		out.print("success");
	}
}

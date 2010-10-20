package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.ServerMethodCaller;

import common.MatchSet;

public class RunServlet extends AbstractServlet {
	private static final long serialVersionUID = -5024779464960322694L;
	public static final String name = "run.html";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String team_a = request.getParameter("team_a");
		String team_b = request.getParameter("team_b");
		if (team_a == null || team_a.trim().equals("")) {
			out.print("err team_a");
			return;
		} else if (team_b == null || team_b.trim().equals("")) {
			out.print("err team_b");
			return;
		} 
		try {
			MatchSet ms = new MatchSet(team_a, team_b);
			ServerMethodCaller.queueRun(ms);
			out.print("success");
		} catch (Exception e) {
			e.printStackTrace(out);
		}

	}
}

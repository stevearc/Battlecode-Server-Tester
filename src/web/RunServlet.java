package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.MasterMethodCaller;
import beans.BSUser;


/**
 * Queue up a new run
 * @author stevearc
 *
 */
public class RunServlet extends AbstractServlet {
	private static final long serialVersionUID = -5024779464960322694L;
	public static final String NAME = "run.html";
	protected int p;

	public RunServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BSUser user = checkLogin(request, response);
		if (user == null) {
			redirect(response);
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String team_a = request.getParameter("team_a");
		String team_b = request.getParameter("team_b");
		String seeds = request.getParameter("seeds");
		String maps = request.getParameter("maps");
		if (team_a == null || team_a.trim().equals("")) {
			out.print("err team_a");
			return;
		} else if (team_b == null || team_b.trim().equals("")) {
			out.print("err team_b");
			return;
		} else if (maps.split(",").length < 1) {
			out.print("err maps");
			return;
		}
		for (String seed: seeds.split(",")) {
			try {
				p = Integer.parseInt(seed);
			} catch (NumberFormatException e) {
				out.print("err seed");
				return;
			}
		}
		try {
			MasterMethodCaller.queueRun(team_a, team_b, seeds.split(","), maps.split(","));
			out.print("success");
		} catch (Exception e) {
			e.printStackTrace(out);
		}

	}
}

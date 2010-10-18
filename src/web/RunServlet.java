package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			PreparedStatement stmt = db.prepare("INSERT INTO queue (team_a, team_b) VALUES (?, ?)");
			stmt.setString(1, team_a);
			stmt.setString(2, team_b);
			stmt.executeUpdate();
			stmt.close();
			out.print("success");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}

	}
}

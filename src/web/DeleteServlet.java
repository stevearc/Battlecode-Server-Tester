package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeleteServlet extends AbstractServlet {
	private static final long serialVersionUID = 4149519483270976451L;
	public static final String name = "delete.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id: " + strId);
		}

		int id = Integer.parseInt(strId);
		try {
			PreparedStatement st = db.prepare("SELECT * FROM runs WHERE finished = 0 AND id = ?");
			st.setInt(1, id);
			ResultSet rs = db.query(st);
			// If the run has not finished
			if (rs.next()) {
				out.print("That match is still running");
				return;
			} 
			PreparedStatement stmt = db.prepare("DELETE FROM matches WHERE run_id = ?");
			stmt.setInt(1, id);
			db.update(stmt, true);
			PreparedStatement stmt2 =  db.prepare("DELETE FROM runs WHERE id = ?");
			stmt2.setInt(1, id);
			db.update(stmt2, true);
			st.close();
			out.print("success");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
	}
}

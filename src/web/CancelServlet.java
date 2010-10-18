package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Config;

public class CancelServlet extends AbstractServlet {
	private static final long serialVersionUID = -462943114244476947L;
	public static final String name = "cancel.html";

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
			// If the run has finished
			if (!rs.next()) {
				out.print("Run already canceled");
				return;
			} 
			PreparedStatement stmt = db.prepare("UPDATE runs SET finished = 1 WHERE id = ?");
			stmt.setInt(1, id);
			db.update(stmt, true);
			Config.getServer().cancelCurrent();
			st.close();
			out.print("success");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
	}
}

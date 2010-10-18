package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DequeueServlet extends AbstractServlet {
	private static final long serialVersionUID = 487791402104374955L;
	public static final String name = "dequeue.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String strId = request.getParameter("id");
		if (strId != null && strId.matches("\\d+")) {
			try {
				int id = Integer.parseInt(strId);
				PreparedStatement st = db.prepare("DELETE FROM queue WHERE id = ?");
				st.setInt(1, id);
				db.update(st, true);
				out.print("success");
			} catch (SQLException e) {
				e.printStackTrace(out);
			}
		} else {
			out.println("Invalid id: " + strId);
		}
	}
}

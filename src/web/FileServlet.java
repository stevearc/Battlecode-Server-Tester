package web;

import java.io.IOException;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileServlet extends AbstractServlet {
	private static final long serialVersionUID = 1535716853886006962L;
	public static final String name = "file.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + request.getParameter("run_id") + 
				request.getParameter("map") + ".rms\"");
		
		response.setStatus(HttpServletResponse.SC_OK);
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			response.getWriter().println("Invalid id: " + strId);
		}

		int id = Integer.parseInt(strId);
		try {
			PreparedStatement st = db.prepare("SELECT data FROM matches WHERE id = ?");
			st.setInt(1, id);
			ResultSet rs = db.query(st);
			// If the run has finished
			if (!rs.next()) {
				response.getWriter().print("Match has no data");
				return;
			} 
			Blob b = rs.getBlob("data");
			response.getOutputStream().write(b.getBytes((long) 1, (int) b.length()));
			st.close();
		} catch (SQLException e) {
			config.getLogger().log(Level.WARNING, "Error serving file", e);
		}
	}
}

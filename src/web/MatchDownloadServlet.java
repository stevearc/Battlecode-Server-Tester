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

/**
 * Serves the replay files
 * @author stevearc
 *
 */
public class MatchDownloadServlet extends AbstractServlet {
	private static final long serialVersionUID = 1535716853886006962L;
	public static final String NAME = "file.html";
	
	public MatchDownloadServlet() {
		super(NAME);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null) {
			redirect(response);
			return;
		}
		response.setContentType("application/octet-stream");

		response.setStatus(HttpServletResponse.SC_OK);
		try {
			String strId = request.getParameter("id");
			if (strId == null || !strId.matches("\\d+")) {
				response.getWriter().println("Invalid id: " + strId);
				return;
			}

			int id = Integer.parseInt(strId);

			PreparedStatement st = db.prepare("SELECT * FROM matches WHERE id = ?");
			st.setInt(1, id);
			ResultSet rs = db.query(st);
			// If the run has finished
			if (!rs.next()) {
				response.getWriter().print("Match has no data");
				return;
			} 
		response.setHeader("Content-Disposition", "attachment; filename=\"" + rs.getInt("run_id") + 
				rs.getString("map") + rs.getInt("seed") + ".rms\"");
			Blob b = rs.getBlob("data");
			response.getOutputStream().write(b.getBytes((long) 1, (int) b.length()));
			st.close();
		} catch (SQLException e) {
			config.getLogger().log(Level.WARNING, "Error serving file", e);
		} catch (IOException e) {
			config.getLogger().log(Level.WARNING, "Error serving file", e);
		}
	}
}

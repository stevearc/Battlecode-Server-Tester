package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutServlet extends AbstractServlet {
	private static final long serialVersionUID = 2965899260728547098L;
	public static final String NAME = "logout.html";

	public LogoutServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null) {
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			response.setHeader("Location", response.encodeURL("/" + LoginServlet.NAME));
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		PreparedStatement st;
		try {
			st = db.prepare("UPDATE users SET session = NULL WHERE username LIKE ?");
			st.setString(1, username);
			db.update(st, true);

			out.println("<script type='text/javascript'>\n" +
					"document.cookie = '" + COOKIE_NAME + "=; expires=Fri, 27 Jul 2001 00:00:00 UTC; path=/';\n" +
					"document.location='" + response.encodeURL(LoginServlet.NAME) + "';\n" +
			"</script>");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
	}
}

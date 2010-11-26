package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Util;

public class LoginServlet extends AbstractServlet {
	private static final long serialVersionUID = 4347939279807133754L;
	public static final String NAME = "login.html";
	private static final char[] BLACKLIST = {' ', '<', '>', '\'', '"', '`', '\t', '\n'};

	public LoginServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username != null) {
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			response.setHeader("Location", response.encodeURL("/" + IndexServlet.NAME));
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String name_param = request.getParameter("username");
		name_param = (name_param == null ? "" : name_param);
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"/css/tinytable.css\" />");
		out.println("<link rel=\"stylesheet\" href=\"/css/tabs.css\" />");
		out.println("</head>");
		out.println("<body>");	
		String background = "#CEFFFC";
		out.println("<div class='center' style='background:" + background + "'>");	
		out.println("<form id='login' method='post'>" +
				"<p>" +
				"<p><label for=\"username\">Username:</label>" +
				"<input type=\"text\" name=\"username\" id=\"username\" value='" + name_param + "' size=\"15\"></p>" +
				"<p><label for=\"password\">Password: </label>" +
				"<input type=\"password\" name=\"password\" id=\"password\" size=\"15\"></p>" + 
				"<input type=\"hidden\" id=\"seed\"></p>" + 
				"<p><label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>" +
				"<input type='submit' value='Login' name='login'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
				"<input type='submit' value='Register' name='register'>" +
				"</p>" +
				"</p></form>"
		);

		String error = (String) request.getAttribute("error");
		if ("no_username".equals(error)) {
			out.println("<font color='red'>Must enter a username</font>");
		} else if ("no_password".equals(error)) {
			out.println("<font color='red'>Must enter a password</font>");
		} else if ("bad_auth".equals(error)) {
			out.println("<font color='red'>Bad username or password</font>");
		} else if ("name_taken".equals(error)) {
			out.println("<font color='red'>Username already taken</font>");
		} else if ("name_length".equals(error)) {
			out.println("<font color='red'>Username is too long</font>");
		} else if ("bad_char".equals(error)) {
			out.print("<font color='red'>Username contains illegal characters (");
			for (char c: BLACKLIST)
				out.print(c);
			out.println(")</font>");
		}

		if ("success".equals(request.getAttribute("register"))) {
			out.println("<font color='red'>Registration successful!  Wait for admin to confirm your credentials</font>");
		}
		out.println("<script type='text/javascript'>\n" +
				"document.getElementById('seed').value=Math.random();\n" +
				"document.getElementById('username').focus();\n" +
		"</script>");

		out.println("</div>");	
		out.println("</body></html>");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username");
		username = (username == null ? null : username.trim());
		String password = request.getParameter("password");
		if (username == null || "".equals(username)) {
			request.setAttribute("error", "no_username");
			doGet(request, response);
			return;
		}
		if (username.length() > 20) {
			request.setAttribute("error", "name_length");
			doGet(request, response);
			return;
		}
		for (char c: BLACKLIST) {
			if (username.indexOf(c) != -1) {
				request.setAttribute("error", "bad_char");
				doGet(request, response);
				return;
			}
		}
		if (password == null || "".equals(password.trim())) {
			request.setAttribute("error", "no_password");
			doGet(request, response);
			return;
		}
		try {
			String seed = request.getParameter("seed");
			seed += Util.SHA1(""+Math.random());
			String salt = Util.SHA1(seed);
			if (request.getParameter("register") != null) {
				// Check to see if username is taken yet
				PreparedStatement stmt = db.prepare("SELECT username FROM users WHERE username LIKE ?");
				stmt.setString(1, username);
				ResultSet rs = db.query(stmt);
				if (rs.next()) {
					request.setAttribute("error", "name_taken");
					doGet(request, response);
					return;
				}
				String hashed_password = Util.SHA1(password + salt);
				PreparedStatement st = db.prepare("INSERT INTO users (username, password, salt, status) " +
				"VALUES (?, ?, ?, ?)");
				st.setString(1, username);
				st.setString(2, hashed_password);
				st.setString(3, salt);
				st.setInt(4, 0);
				db.update(st, true);
				request.setAttribute("register", "success");
				doGet(request, response);
				return;
			}
			if (checkCredentials(username, password)) {
				if (request.getParameter("login") != null) {
					PreparedStatement st = db.prepare("UPDATE users SET session = ? WHERE username LIKE ?");
					st.setString(1, salt);
					st.setString(2, username);
					db.update(st, true);
					Cookie c = new Cookie(COOKIE_NAME, salt);
					c.setSecure(true);
					response.addCookie(c);
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("text/html");
					goToPage(response, IndexServlet.NAME);
					return;
				}
			}
		} catch (Exception e) {
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			e.printStackTrace(out);
		}
		request.setAttribute("error", "bad_auth");
		doGet(request, response);
	}

	private boolean checkCredentials(String username, String password) throws SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {
		PreparedStatement st;
		st = db.prepare("SELECT * FROM users WHERE username LIKE ?");
		st.setString(1, username);
		ResultSet rs = db.query(st);
		if (!rs.next())
			return false;
		String salt = rs.getString("salt");
		String hashed = Util.SHA1(password + salt);
		if (!hashed.equals(rs.getString("password")))
			return false;
		return true;
	}

	private void goToPage(HttpServletResponse response, String page) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<script type='text/javascript'>document.location='" + response.encodeURL(page) + "'</script>");
		out.println("</head></html>");
	}
}

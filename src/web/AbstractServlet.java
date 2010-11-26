package web;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Config;

import db.Database;

public abstract class AbstractServlet extends HttpServlet {
	private static final long serialVersionUID = 7415725707009960270L;
	protected static final String COOKIE_NAME = "bs-tester";
	public final String name;
	protected Database db;
	protected Config config;

	public AbstractServlet(String name) {
		this.name = name;
		config = Config.getConfig();
		db = Config.getDB();
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @return True if authenticated, false otherwise
	 * @throws IOException 
	 */
	protected String checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Cookie auth = null;
		String username = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c: cookies) {
				if (COOKIE_NAME.equals(c.getName())) {
					auth = c;
					break;
				}
			}
		}
		if (auth == null) {
			return username;
		}
		// Check cookie value
		try {
			PreparedStatement st = db.prepare("SELECT username FROM users WHERE session LIKE ?");
			st.setString(1, auth.getValue());
			ResultSet rs = db.query(st);
			if (rs.next()) {
				username = rs.getString("username");
			}
		} catch (SQLException e) {
			e.printStackTrace(response.getWriter());
			return username;
		}
		return username;
	}
	
	protected int getStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Cookie auth = null;
		int status = 0;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c: cookies) {
				if (COOKIE_NAME.equals(c.getName())) {
					auth = c;
					break;
				}
			}
		}
		if (auth == null) {
			return status;
		}
		// Check cookie value
		try {
			PreparedStatement st = db.prepare("SELECT status FROM users WHERE session LIKE ?");
			st.setString(1, auth.getValue());
			ResultSet rs = db.query(st);
			if (rs.next()) {
				status = rs.getInt("status");
			}
		} catch (SQLException e) {
			e.printStackTrace(response.getWriter());
			return status;
		}
		return status;
	}

	protected void redirect(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", response.encodeURL("/" + LoginServlet.NAME));
	}

}

package web;

import java.io.IOException;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSUser;


import common.Config;

import dataAccess.HibernateUtil;

/**
 * Base framework for all servlets
 * @author stevearc
 *
 */
public abstract class AbstractServlet extends HttpServlet {
	private static final long serialVersionUID = 7415725707009960270L;
	protected static final String COOKIE_NAME = "bs-tester";
	public final String name;
	protected Config config;

	/**
	 * 
	 * @param name URL of servlet
	 */
	public AbstractServlet(String name) {
		this.name = name;
		config = Config.getConfig();
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @return True if authenticated, false otherwise
	 * @throws IOException 
	 */
	protected BSUser checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String cookieVal = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c: cookies) {
				if (COOKIE_NAME.equals(c.getName())) {
					cookieVal = c.getValue();
					break;
				}
			}
		}
		if (cookieVal == null) {
			return null;
		}
		// Cookie is encoded as [userid]$[session token]
		Long userId = new Long(-1);
		String token = "";
		try {
			userId = new Long(Integer.parseInt(cookieVal.substring(0, cookieVal.indexOf("$"))));
			token = cookieVal.substring(cookieVal.indexOf("$") + 1);
		} catch (Exception e) {
			config.getLogger().log(Level.WARNING, "Login cookie with bad format: " + cookieVal, e);
		}
		// Check cookie value
		EntityManager em = HibernateUtil.getEntityManager();
		BSUser user = em.find(BSUser.class, userId);
		if (user == null) {
			return null;
		}
		if (token.equals(user.getSession())) {
			return user;
		}
			
		return null;
	}
	
	protected void redirect(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", response.encodeURL("/" + LoginServlet.NAME));
	}

}

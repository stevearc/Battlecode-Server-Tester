package web;

import javax.servlet.http.HttpServlet;

import common.Config;

import db.Database;

public abstract class AbstractServlet extends HttpServlet {
	private static final long serialVersionUID = 7415725707009960270L;
	protected Database db;
	protected Config config;

	public AbstractServlet() {
		config = Config.getConfig();
		db = Config.getDB();
	}
	
}

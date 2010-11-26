package db;

import java.sql.DriverManager;
import java.sql.SQLException;

public class HSQLDatabase extends JDBCDatabase {

	private static String[] createStmts = {
		//
		"CREATE TABLE matches (id IDENTITY, run_id INTEGER NOT NULL, map varchar(30) NOT NULL, seed INTEGER NOT NULL, win TINYINT, win_condition INTEGER, " +
		//
		"height INTEGER NOT NULL, width INTEGER NOT NULL, rounds INTEGER NOT NULL, points INTEGER NOT NULL, a_points INTEGER, b_points INTEGER, data BLOB)",
		//
		"CREATE TABLE runs (id IDENTITY,team_a varchar(45) NOT NULL, team_b varchar(45) NOT NULL,status TINYINT DEFAULT '0', started timestamp,ended timestamp)",
		//
		"CREATE TABLE tags (tag varchar(45) NOT NULL,alias varchar(20) DEFAULT NULL,UNIQUE (tag))",
		//  status (0 - unapproved, 1 - active, 2 - admin)
		"CREATE TABLE users (username varchar(20) NOT NULL, password varchar(40) NOT NULL, salt varchar(40) NOT NULL, session varchar(40), status TINYINT NOT NULL, UNIQUE(username))",
	};
	
	private static String[] dropStmts = {
		"DROP TABLE IF EXISTS matches",
		"DROP TABLE IF EXISTS runs",
		"DROP TABLE IF EXISTS tags",
		"DROP TABLE IF EXISTS users",
	};

	@Override
	public void connect() throws Exception {
		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:hsqldb:file:" + config.install_dir + "/hsqldb" + config.db_host,config.db_user,config.db_pass);
		} catch (Exception e) {
			_log.severe("Cannot connect to database");
			throw e;
		}
		if (config.reset_db) {
			drop();
		}
		init();
	}
	
	private void drop() throws SQLException {
        for (String stmt: dropStmts) {
			update(stmt, false);
		}
	}
	
	private void init() throws SQLException {
        for (String stmt: createStmts) {
			update(stmt, false);
		}
	}

}

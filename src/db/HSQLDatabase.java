package db;

import java.sql.DriverManager;
import java.sql.SQLException;

public class HSQLDatabase extends JDBCDatabase {

	private static String[] createStmts = {
		"CREATE TABLE connections (id IDENTITY, addr varchar(60) DEFAULT NULL)",
		"CREATE TABLE matches (id IDENTITY, run_id INTEGER NOT NULL,map varchar(30) NOT NULL, win TINYINT NOT NULL, data BLOB NOT NULL)",
		"CREATE TABLE queue (id IDENTITY,team_a varchar(45) DEFAULT NULL,team_b varchar(45) DEFAULT NULL,)",
		"CREATE TABLE running_matches (conn_id INTEGER NOT NULL,map varchar(50) NOT NULL,modified timestamp DEFAULT CURRENT_TIMESTAMP)",
		"CREATE TABLE runs (id IDENTITY,team_a varchar(45) NOT NULL,team_b varchar(45) NOT NULL,started timestamp NOT NULL,finished TINYINT DEFAULT '0',ended timestamp DEFAULT CURRENT_TIMESTAMP)",
		"CREATE TABLE tags (tag varchar(45) NOT NULL,alias varchar(20) DEFAULT NULL,UNIQUE (tag))"
	};
	
	private static String[] dropStmts = {
		"DROP TABLE IF EXISTS connections",
		"DROP TABLE IF EXISTS matches",
		"DROP TABLE IF EXISTS queue",
		"DROP TABLE IF EXISTS running_matches",
		"DROP TABLE IF EXISTS runs",
		"DROP TABLE IF EXISTS tags"
	};

	@Override
	public void connect() throws Exception {
		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:hsqldb:file:" + config.home + "/hsqldb" + config.db_host,config.db_user,config.db_pass);
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
			update(stmt, true);
		}
	}
	
	private void init() throws SQLException {
        for (String stmt: createStmts) {
			update(stmt, false);
		}
	}
}

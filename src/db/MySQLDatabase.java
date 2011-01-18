package db;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase extends JDBCDatabase {

	private static String[] createStmts = {
		// map (map name), seed (seed to use for map), win (1 - team a, 0 - team b), win_condition (0 - team destroyed, 1 - points, 2 - time/other)
		// height (height of map), width (width of map), rounds (map's hardcoded rounds), points (map's hardcoded points), data (replay file)
		"CREATE TABLE IF NOT EXISTS `matches` (`id` int(11) NOT NULL AUTO_INCREMENT, `run_id` int(11) NOT NULL,`map` varchar(30) NOT NULL, " +
		"`seed` int(11) NOT NULL, `win` int(1), `win_condition` int(11), `height` int(11) NOT NULL, `width` int(11) NOT NULL, `rounds` int(11) NOT NULL, " +
		"`a_points` decimal(6,2), `b_points` decimal(6,2), `data` mediumblob, PRIMARY KEY (`id`))",
		// status (0 - queued, 1 - running, 2 - complete, 3 - error, 4 - canceled)
		"CREATE TABLE IF NOT EXISTS `runs` (`id` int(11) NOT NULL AUTO_INCREMENT,`team_a` varchar(45) NOT NULL,`team_b` varchar(45) NOT NULL," +
		"`status` int(11) DEFAULT '0',`started` timestamp DEFAULT 0,`ended` timestamp ON UPDATE current_timestamp,PRIMARY KEY (`id`))",
		// allows svn users to easily make an alias for a specific revision of code
		"CREATE TABLE IF NOT EXISTS `tags` (`tag` varchar(45) NOT NULL,`alias` varchar(20) DEFAULT NULL,UNIQUE KEY `tag` (`tag`))", 
		// session (cookie auth value), status (0 - unapproved, 1 - active, 2 - admin)
		"CREATE TABLE IF NOT EXISTS `users` (`username` varchar(20) NOT NULL,`password` varchar(40) NOT NULL,`salt` varchar(40) NOT NULL,`session` varchar(40)," +
		"`status` int(1) NOT NULL,UNIQUE KEY `username` (`username`))", 
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
			conn = DriverManager.getConnection("jdbc:mysql://" + config.db_host,config.db_user,config.db_pass);
		} catch (Exception e) {
			_log.severe("Cannot connect to database");
			throw e;
		}
		update("use " + config.db_name, true);
		init();
	}
	
	@SuppressWarnings("unused")
	private void drop() throws SQLException {
        for (String stmt: dropStmts) {
			update(stmt, true);
		}
	}
	
	private void init() throws SQLException {
        for (String stmt: createStmts) {
			update(stmt, true);
		}
	}
}

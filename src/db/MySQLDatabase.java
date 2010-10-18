package db;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase extends JDBCDatabase {

	private static String[] createStmts = {
		"CREATE TABLE IF NOT EXISTS `connections` (`id` int(11) NOT NULL AUTO_INCREMENT, `addr` varchar(60) DEFAULT NULL, PRIMARY KEY (`id`))",
		"CREATE TABLE IF NOT EXISTS `matches` (`id` int(11) NOT NULL AUTO_INCREMENT, `run_id` int(11) NOT NULL,`map` varchar(30) NOT NULL, `win` int(1) NOT NULL, `data` mediumblob NOT NULL, PRIMARY KEY (`id`))",
		"CREATE TABLE IF NOT EXISTS `queue` (`id` int(11) NOT NULL AUTO_INCREMENT,`team_a` varchar(45) DEFAULT NULL,`team_b` varchar(45) DEFAULT NULL,PRIMARY KEY (`id`))",
		"CREATE TABLE IF NOT EXISTS `running_matches` (`conn_id` int(11) NOT NULL,`map` varchar(50) NOT NULL,`modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
		"CREATE TABLE IF NOT EXISTS `runs` (`id` int(11) NOT NULL AUTO_INCREMENT,`team_a` varchar(45) NOT NULL,`team_b` varchar(45) NOT NULL,`started` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',`finished` int(11) DEFAULT '0',`ended` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,PRIMARY KEY (`id`))",
		"CREATE TABLE IF NOT EXISTS `tags` (`tag` varchar(45) NOT NULL,`alias` varchar(20) DEFAULT NULL,UNIQUE KEY `tag` (`tag`))"
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
			conn = DriverManager.getConnection("jdbc:mysql://" + config.db_host,config.db_user,config.db_pass);
		} catch (Exception e) {
			_log.severe("Cannot connect to database");
			throw e;
		}
		update("use " + config.db_name, true);
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
			update(stmt, true);
		}
	}
}

package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Config;

public abstract class JDBCDatabase implements Database {
	protected Connection conn;
	protected Logger _log;
	protected Config config;

	protected JDBCDatabase() {
		config = Config.getConfig();
		_log = config.getLogger();
	}

	@Override
	public ResultSet query(String sql) throws SQLException {
		PreparedStatement st = prepare(sql);
		return query(st);
	}

	@Override
	public ResultSet query(PreparedStatement stmt) throws SQLException {
		try {
			ResultSet rs = stmt.executeQuery();
			return rs;
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "SQL query failed", e);
			throw e;
		} 
	}

	@Override
	public void update(String sql, boolean fatal) throws SQLException {
		PreparedStatement st = prepare(sql);
		update(st, fatal);
	}

	@Override
	public void update(PreparedStatement stmt, boolean fatal) throws SQLException {
		try {
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			if (fatal) {
				_log.log(Level.SEVERE, "SQL update failed", e);
				throw e;
			}
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e){}
			}
		}
	}

	@Override
	public PreparedStatement prepare(final String sql) throws SQLException {
		try {
			return conn.prepareStatement(sql);
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "Error preparing statement: " + sql, e);
			throw e;
		}
	}

	@Override
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			_log.log(Level.WARNING, "Cannot close database", e);
		}
	}

}

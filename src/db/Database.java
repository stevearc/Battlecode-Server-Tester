package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for interacting with the database
 * @author stevearc
 *
 */
public interface Database {
	
	/**
     *  Connect to the database
     */
    public void connect() throws Exception;

    /**
     *  Execute a database query
     *  @param sql The SQL to run on the db
     *  @return The ResultSet from the query
     *  @throws SQLException
     */
    public ResultSet query(final String sql) throws SQLException;
    
    /**
     *  Execute a database query
     *  @param sql The SQL to run on the db
     *  @return The ResultSet from the query
     *  @throws SQLException
     */
    public ResultSet query(PreparedStatement sql) throws SQLException;
    
    /**
     *  performs an update on the database
     *  @param sql the sql to execute
     *  @param fatal if true, will throw exceptions on failure
     *  @throws SQLException
     */
    public void update(final String sql, boolean fatal) throws SQLException;
    
    /**
     *  performs an update on the database
     *  @param sql the sql to execute
     *  @param fatal if true, will throw exceptions on failure
     *  @throws SQLException
     */
    public void update(PreparedStatement stmt, boolean fatal) throws SQLException;
    
    /**
     *  shuts down the database and closes the connection
     * 
     */
    public void close();
    
    /**
     *  returns a prepared statement for the specified sql
     *  @param sql
     *  @return Statement
     * 
     */
    public PreparedStatement prepare( final String sql ) throws SQLException;
    
}

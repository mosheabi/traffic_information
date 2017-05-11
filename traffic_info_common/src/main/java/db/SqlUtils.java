package db;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class SqlUtils {
	

	// logger
	private static Logger log = Logger.getLogger(SqlUtils.class);


	//=======================
	public static void close(Statement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException sqle) {
			if (log.isDebugEnabled()) {
				log.error("SQLException while trying to close statement");
			}
		}
	}

	
	//=======================
	public static void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException sqle) {
			if (log.isDebugEnabled()) {
				log.error("SQLException while trying to close resultset");
			}
		}
	}


	//=======================
	public static void close(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException sqle) {
			if (log.isDebugEnabled()) {
				log.error("SQLException while trying to close connection");
			}
		}
	}

	public static void closeAll(Connection con, Statement stmt, ResultSet rs) {
		close(rs);
		close(stmt);
		close(con);
	}
}

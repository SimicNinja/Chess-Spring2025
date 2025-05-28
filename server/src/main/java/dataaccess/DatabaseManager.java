package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager
{
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information for the db.properties file.
     */
    static
    {
        try
        {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties"))
            {
                if (propStream == null)
                {
                    throw new Exception("Unable to load db.properties");
                }
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException
    {
        var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
        try (var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD); var preparedStatement = conn.prepareStatement(statement))
        {
            preparedStatement.executeUpdate();

            conn.setCatalog(DATABASE_NAME);

            var createAuthTable = """
				CREATE TABLE IF NOT EXISTS authData(
					authToken varchar(255) NOT NULL,
					username varchar(255) NOT NULL
				) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin""";
            var createUserTable = """
				CREATE TABLE IF NOT EXISTS userData(
					username varchar(255) NOT NULL UNIQUE,
					password varchar(255) NOT NULL,
					email varchar(255) NOT NULL,
					CONSTRAINT username_not_empty CHECK (username <> ''),
					CONSTRAINT password_not_empty CHECK (password <> ''),
					CONSTRAINT email_not_empty CHECK (email <> '')
				) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin""";
            var createGameTable = """
				CREATE TABLE IF NOT EXISTS gameData(
					gameID INT NOT NULL AUTO_INCREMENT,
					whiteUsername VARCHAR(255),
					blackUsername VARCHAR(255),
					gameName VARCHAR(255) NOT NULL UNIQUE,
					game LONGTEXT NOT NULL,
					PRIMARY KEY (gameID),
					CONSTRAINT check_not_empty CHECK (gameName <> '')
				) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin""";

            createTable(conn, createAuthTable);
            createTable(conn, createUserTable);
            createTable(conn, createGameTable);
        }
        catch (SQLException ex)
        {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    public static Connection getConnection() throws DataAccessException
    {
        try
        {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        }
        catch (SQLException ex)
        {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    public static void createTable(Connection conn, String sql)
    {
        try (var createTableStatement = conn.prepareStatement(sql))
        {
            createTableStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}

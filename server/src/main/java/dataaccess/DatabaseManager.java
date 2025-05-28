package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager
{
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static
    {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException
    {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword); var preparedStatement = conn.prepareStatement(statement))
        {
            preparedStatement.executeUpdate();

            conn.setCatalog(databaseName);

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
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        }
        catch (SQLException ex)
        {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources()
    {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties"))
        {
            if (propStream == null)
            {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props)
    {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}

package dataaccess;

import dataaccess.interfaces.AuthDAO;
import dataaccess.mysqldaos.AuthDAOMySQL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthDAOTests
{
	private static Connection conn;
	private final AuthDAO dao = new AuthDAOMySQL();

	@BeforeEach
	public void setup() throws DataAccessException, SQLException
	{
		DatabaseManager.createDatabase();
		conn = DatabaseManager.getConnection();

		String[] auths = {"qwerty", "12345", "asdf;"};
		String[] usernames = {"LickyFrog", "SimicNinja", "JOA"};

		String insert = "INSERT INTO authData (authToken, username) VALUES (?, ?)";

		try(PreparedStatement statement = conn.prepareStatement(insert))
		{
			for(int i = 0; i < 3; i++)
			{
				statement.setString(1, auths[i]);
				statement.setString(2, usernames[i]);
				statement.executeUpdate();
			}
		}
	}

	@AfterEach
	public void tearDown() throws SQLException
	{
		try(PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE userData"))
		{
			statement.executeUpdate();
		}

		conn.close();
		conn = null;
	}

	public static List<String> getItems(String tableName, String column, Connection connect) throws SQLException
	{
		String sql = "SELECT " + column + " from " + tableName;

		try(PreparedStatement statement = connect.prepareStatement(sql))
		{
			List<String> output = new ArrayList<>();
			try(ResultSet set = statement.executeQuery())
			{
				while(set.next())
				{
					String item = set.getString(1);
					output.add(item);
				}
			}

			return output;
		}
	}

	@Test
	public void testCreatAuth() throws SQLException, DataAccessException
	{
		dao.createAuth("Asuna");

		List<String> usernames = getItems("authData", "username", conn);

		Assertions.assertTrue(usernames.contains("Asuna"));
	}

	@Test
	public void testCreateAuthFail()
	{
		Assertions.assertThrows(RuntimeException.class, () -> dao.createAuth(null));
	}

	@Test
	public void testAuthorizeToken() throws SQLException, DataAccessException
	{
		Assertions.assertEquals("SimicNinja", dao.authorizeToken("12345"));
		Assertions.assertEquals("LickyFrog", dao.authorizeToken("qwerty"));
		Assertions.assertEquals("JOA", dao.authorizeToken("asdf;"));
	}

	@Test
	public void testAuthorizeTokenFail() throws SQLException, DataAccessException
	{
		Assertions.assertThrows(DataAccessException.class, () -> dao.authorizeToken("asdf"));
	}

	@Test
	public void testDelete() throws DataAccessException, SQLException
	{
		List<String> usernames;

		dao.deleteAuthData("asdf;");
		usernames = getItems("authData", "username", conn);
		Assertions.assertFalse(usernames.contains("JOA"));

		dao.deleteAuthData("12345");
		usernames = getItems("authData", "username", conn);
		Assertions.assertFalse(usernames.contains("SimicNinja"));
	}

	@Test
	public void testDeleteFail() throws DataAccessException, SQLException
	{
		Assertions.assertThrows(DataAccessException.class, () -> dao.deleteAuthData("67890"));
	}

	@Test
	public void testClear() throws SQLException
	{
		dao.clear();

		List<String> usernames = getItems("authData", "username", conn);

		Assertions.assertTrue(usernames.isEmpty());
	}
}

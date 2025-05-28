package dataaccess;

import dataaccess.interfaces.UserDAO;
import dataaccess.mysqldaos.UserDAOMySQL;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserDAOTests
{
	private static Connection conn;
	private final UserDAO dao = new UserDAOMySQL();

	@BeforeEach
	public void setup() throws DataAccessException, SQLException
	{
		DatabaseManager.createDatabase();
		dao.clear();
		conn = DatabaseManager.getConnection();

		String[] usernames = {"LickyFrog", "SimicNinja", "JOA"};
		String[] passwords = {"kitchenTime", "codingTime", "animeTime"};
		String[] emails = {"1@gmail.com", "2@gmail.com", "3@gmail.com"};

		String insert = "INSERT INTO userData (username, password, email) VALUES(?, ?, ?)";

		try(PreparedStatement statement = conn.prepareStatement(insert))
		{
			for(int i = 0; i < 3; i++)
			{
				statement.setString(1, usernames[i]);
				statement.setString(2, passwords[i]);
				statement.setString(3, emails[i]);
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

	@Test
	public void testCreateUser() throws DataAccessException, SQLException
	{
		dao.createUser("Asuna", "artTime", "4@gmail.com");

		List<String> usernames = AuthDAOTests.getItems("userData", "username", conn);

		Assertions.assertTrue(usernames.contains("Asuna"));
	}

	@Test
	public void testCreateUserFail() throws DataAccessException, SQLException
	{
		dao.createUser("Asuna", "artTime", "4@gmail.com");

		Assertions.assertThrows(DataAccessException.class, () ->
			dao.createUser(null, "motorcycleTime", "5@gmail.com"));

		Assertions.assertThrows(DataAccessException.class, () ->
				dao.createUser("Asuna", "", "5@gmail.com"));

		Assertions.assertThrows(RuntimeException.class, () ->
				dao.createUser("Asuna", "motorcycleTime", "5@gmail.com"));
	}

	@Test
	public void testGetUser()
	{
		Assertions.assertEquals(new UserData("SimicNinja", "codingTime", "2@gmail.com"),
				dao.getUser("SimicNinja"));
		Assertions.assertEquals(new UserData("LickyFrog", "kitchenTime", "1@gmail.com"),
				dao.getUser("LickyFrog"));
		Assertions.assertEquals(new UserData("JOA", "animeTime", "3@gmail.com"),
				dao.getUser("JOA"));
	}

	@Test
	public void testGetUserFail()
	{
		Assertions.assertNull(dao.getUser("Asuna"));
		Assertions.assertNull(dao.getUser("lickyfrog")); //Check for case-sensitivity.
	}

	@Test
	public void testUserClear() throws SQLException
	{
		dao.clear();

		List<String> usernames = AuthDAOTests.getItems("userData", "username", conn);

		Assertions.assertTrue(usernames.isEmpty());
	}
}

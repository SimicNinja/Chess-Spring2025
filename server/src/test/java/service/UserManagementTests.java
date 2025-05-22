package service;

import dataaccess.DataAccessException;
import model.Records.LoginRequest;
import model.Records.LoginResult;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagementTests
{
	private static DAOManagement daoManager;
	private static UserManagement userManager;

	@BeforeAll
	public static void init()
	{
		daoManager = new DAOManagement();
		userManager = new UserManagement(daoManager);
	}

	@BeforeEach
	public void setup()
	{
		try
		{
			daoManager.clearApplication();
			userManager.register(new UserData("LickyFrog", "greenTreeFrog", "amazon@gmail.com"));
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Successful User Registration")
	public void lickyFrog()
	{
		daoManager.clearApplication();

		try
		{
			LoginResult result = userManager.register(new UserData("LickyFrog", "greenTreeFrog", "amazon@gmail.com"));

			assertNotNull(result, "Result should not be null.");
			assertEquals("LickyFrog", result.username(), "Username should match the registered user.");
			assertNotNull(result.authToken(), "AuthToken should not be null.");
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Insufficient User Registration Information")
	public void missingPassword()
	{
		UserData noPasswordStan = new UserData("Stan", "", "stan.lee@hotmail.com");

		Exception e = assertThrows(DataAccessException.class, () ->	userManager.register(noPasswordStan));

		assertTrue(e.getMessage().contains("You must provide a username, password, & email."));
	}

	@Test
	@DisplayName("Duplicate User")
	public void secondFrog()
	{
		UserData secondFrog = new UserData("LickyFrog", "brownTreeFrog", "florida@gmail.com");

		Exception exception = assertThrows(DataAccessException.class, () ->	userManager.register(secondFrog));

		assertTrue(exception.getMessage().contains("already exists"));
	}

	@Test
	@DisplayName("Successful Login")
	public void loginTest()
	{
		try
		{
			LoginResult result = userManager.login(new LoginRequest("LickyFrog", "greenTreeFrog"));

			assertNotNull(result);
			assertEquals("LickyFrog", result.username());
			assertNotNull(result.authToken());
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Wrong Password")
	public void badPasswordTest()
	{
		LoginRequest wrongPassword = new LoginRequest("LickyFrog", "brownTreeFrog");

		Exception e = assertThrows(DataAccessException.class, () ->	userManager.login(wrongPassword));
		assertTrue(e.getMessage().contains("Incorrect password for LickyFrog"));
	}

	@Test
	@DisplayName("Non-existent User")
	public void noUserTest()
	{
		LoginRequest unregisteredUser = new LoginRequest("JohnDoe", "brownTreeFrog");

		Exception e = assertThrows(DataAccessException.class, () ->	userManager.login(unregisteredUser));
		assertTrue(e.getMessage().contains("User JohnDoe does not exist."));
	}

	@Test
	@DisplayName("Successful Logout")
	public void logoutTest()
	{
		daoManager.clearApplication();

		try
		{
			LoginResult result = userManager.register(new UserData("Stan", "1324", "stan.lee@hotmail.com"));
			String authToken = result.authToken();

			userManager.logout(authToken);

			assertTrue(daoManager.getAuthorizations().isEmpty(), "Authorizations isn't empty.");
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Double Logout")
	public void doubleLogoutTest()
	{
		daoManager.clearApplication();

		try
		{
			LoginResult result = userManager.register(new UserData("Stan", "1324", "stan.lee@hotmail.com"));
			String authToken = result.authToken();

			userManager.logout(authToken);

			Exception e = assertThrows(DataAccessException.class, () ->	userManager.logout(authToken));
			assertTrue(e.getMessage().contains("no authorization token"));
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}

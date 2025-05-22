package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DAOManagementTests
{
	@Test
	@DisplayName("Successful Application Clear")
	public void clearTest()
	{
		DAOManagement daoManager = new DAOManagement();
		UserManagement userManager = new UserManagement(daoManager);

		try
		{
			userManager.register(new UserData("LickyFrog", "greenTreeFrog", "amazon@gmail.com"));
			userManager.register(new UserData("Stan", "1324", "stan.lee@hotmail.com"));
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}

		daoManager.clearApplication();

		assertTrue(daoManager.getUsers().isEmpty(), "Users isn't empty.");
		assertTrue(daoManager.getAuthorizations().isEmpty(), "Authorizations isn't empty.");
	}
}

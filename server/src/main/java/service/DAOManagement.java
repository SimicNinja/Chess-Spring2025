package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import dataaccess.memorydaos.*;
//import dataaccess.mysqldaos.*;

public class DAOManagement
{
	private final GameDAO games = new GameDAOMemory();
	private final UserDAO users = new UserDAOMemory();
	private final AuthDAO authorizations = new AuthDAOMemory();

	public DAOManagement()
	{
		//For MySQL implementation only.
//		try
//		{
//			DatabaseManager.createDatabase();
//		}
//		catch(DataAccessException e)
//		{
//			throw new RuntimeException(e);
//		}
	}

	protected UserDAO getUsers()
	{
		return users;
	}

	protected AuthDAO getAuthorizations()
	{
		return authorizations;
	}

	protected GameDAO getGames()
	{
		return games;
	}

	public void clearApplication()
	{
		games.clear();
		users.clear();
		authorizations.clear();
	}
}

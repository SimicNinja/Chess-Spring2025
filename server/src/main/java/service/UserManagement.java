package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;
import model.Records.LoginRequest;
import model.Records.LoginResult;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class UserManagement
{
	private final AuthDAO authorizations;
	private final UserDAO users;

	public UserManagement(DAOManagement daoManager)
	{
		this.users = daoManager.getUsers();
		this.authorizations = daoManager.getAuthorizations();
	}

	public LoginResult register(UserData registerRequest) throws DataAccessException
	{
		String username = registerRequest.username();
		if(users.getUser(username) == null)
		{
			users.createUser(username, registerRequest.password(), registerRequest.email());
			return login(username);
		}
		throw new DataAccessException("User " + username + "already exists.");
	}


	public LoginResult login(LoginRequest request) throws DataAccessException
	{
		String username = request.username();
		String password = request.password();

		if(username == null || password == null)
		{
			throw new DataAccessException("You must provide a username & password");
		}
		else if(users.getUser(username) == null)
		{
			throw new DataAccessException("User " + username + " does not exist.");
		}
		else if(!BCrypt.checkpw(password, users.getUser(username).password()))
		{
			throw new DataAccessException("Incorrect password for " + username);
		}
		else
		{
			return login(username);
		}
	}

	private LoginResult login(String username) throws DataAccessException
	{
		return new LoginResult(username, authorizations.createAuth(username));
	}

	public void logout(String authToken) throws DataAccessException
	{
		authorizations.deleteAuthData(authToken);
	}
}

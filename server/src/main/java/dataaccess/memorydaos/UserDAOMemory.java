package dataaccess.memorydaos;

import dataaccess.DataAccessException;
import dataaccess.interfaces.UserDAO;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemory implements UserDAO
{
	private Map<String, UserData> userMap = new HashMap<>();

	public UserData getUser(String username)
	{
		return userMap.get(username);
	}

	public void createUser(String username, String password, String email) throws DataAccessException
	{
		if(username == null || username.isEmpty() || password == null || password.isEmpty() || email == null || email.isEmpty())
		{
			throw new DataAccessException("You must provide a username, password, & email.");
		}

		String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

		userMap.put(username, new UserData(username, hashedPassword, email));
	}

	public void clear()
	{
		userMap.clear();
	}

	public boolean isEmpty()
	{
		return userMap.isEmpty();
	}
}

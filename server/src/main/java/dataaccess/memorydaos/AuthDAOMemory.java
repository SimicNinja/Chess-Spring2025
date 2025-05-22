package dataaccess.memorydaos;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AuthDAOMemory implements AuthDAO
{
	private final Map<String, AuthData> authMap = new HashMap<>();

	@Override
	public String createAuth(String username)
	{
		String authToken = UUID.randomUUID().toString();
		authMap.put(authToken, new AuthData(authToken, username));
		return authToken;
	}

	@Override
	public String authorizeToken(String authToken) throws DataAccessException
	{
		if(!authMap.containsKey(authToken))
		{
			throw new DataAccessException("There is no authorization token " + authToken + " stored in cache; please login.");
		}
		return authMap.get(authToken).username();
	}

	@Override
	public void deleteAuthData(String authToken) throws DataAccessException
	{
		authorizeToken(authToken);
		authMap.remove(authToken);
	}

	@Override
	public void clear()
	{
		authMap.clear();
	}

	@Override
	public boolean isEmpty()
	{
		return authMap.isEmpty();
	}
}
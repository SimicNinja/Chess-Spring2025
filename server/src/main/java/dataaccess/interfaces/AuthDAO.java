package dataaccess.interfaces;

import dataaccess.DataAccessException;

public interface AuthDAO
{
	public String createAuth(String username) throws DataAccessException;
	public String authorizeToken(String authToken) throws DataAccessException;
	public void deleteAuthData(String authToken) throws DataAccessException;
	public void clear();
	public boolean isEmpty();
}

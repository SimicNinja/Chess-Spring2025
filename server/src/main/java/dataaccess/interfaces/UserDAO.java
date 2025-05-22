package dataaccess.interfaces;

import dataaccess.DataAccessException;
import model.UserData;

public interface UserDAO
{
	public UserData getUser(String username);
	public void createUser(String username, String password, String email) throws DataAccessException;
	public void clear();
	public boolean isEmpty();
}

package dataaccess.mysqldaos;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.interfaces.UserDAO;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

public class UserDAOMySQL extends DAOMySQL implements UserDAO
{
	private final String table = "userData";

	@Override
	public UserData getUser(String username)
	{
		String sql = "SELECT * FROM " + table + " WHERE username = ?";

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				statement.setString(1, username);

				try(var rs = statement.executeQuery())
				{
					if(rs.next())
					{
						return new UserData(rs.getString("username"),
											rs.getString("password"),
											rs.getString("email"));
					}
					else
					{
						return null;
					}
				}
			}
		}
		catch(SQLException | DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void createUser(String username, String password, String email) throws DataAccessException
	{
		String sql = "INSERT INTO " + table + " (username, password, email) VALUES(?, ?, ?)";
		String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

		if(password == null || password.equals(""))
		{
			throw new DataAccessException("You must provide a username, password, & email.");
		}

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var preparedStatement = conn.prepareStatement(sql))
			{
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, hashedPassword);
				preparedStatement.setString(3, email);

				preparedStatement.executeUpdate();
			}
			catch(SQLException e)
			{
				if(e.getMessage().contains("null"))
				{
					throw new DataAccessException("You must provide a username, password, & email.");
				}
				else
				{
					throw e;
				}
			}
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void clear()
	{
		super.clear(table);
	}

	@Override
	public boolean isEmpty()
	{
		return  super.isEmpty(table);
	}
}

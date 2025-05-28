package dataaccess.mysqldaos;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.interfaces.AuthDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class AuthDAOMySQL extends DAOMySQL implements AuthDAO
{
	private final String tableName = "authData";

	@Override
	public String createAuth(String username) throws DataAccessException
	{
		String authToken = UUID.randomUUID().toString();

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var preparedStatement = conn.prepareStatement("INSERT INTO " + tableName +
					" (authToken, username) VALUES(?, ?)"))
			{
				preparedStatement.setString(1, authToken);
				preparedStatement.setString(2, username);

				preparedStatement.executeUpdate();

				return authToken;
			}
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String authorizeToken(String authToken) throws DataAccessException
	{
		String sql = "SELECT username FROM " + tableName + " WHERE authToken = ?";

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				statement.setString(1, authToken);

				try(var rs = statement.executeQuery())
				{
					if(rs.next())
					{
						return rs.getString("username");
					}
					else
					{
						throw new DataAccessException("There is no authorization token " + authToken +
								" stored in the database; please login.");
					}
				}
			}
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deleteAuthData(String authToken) throws DataAccessException
	{
		String sql = "DELETE FROM " + tableName + " WHERE authToken = ?";

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				statement.setString(1, authToken);

				int rowsAffected = statement.executeUpdate();

				if (rowsAffected == 0)
				{
					throw new DataAccessException("There is no authorization token "
							+ authToken + " stored in cache; please login.");
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
		super.clear(tableName);
	}

	public boolean isEmpty()
	{
		return super.isEmpty(tableName);
	}
}

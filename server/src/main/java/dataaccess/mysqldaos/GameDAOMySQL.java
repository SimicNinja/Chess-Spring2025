package dataaccess.mysqldaos;

import chess.ChessGame;
import chess.ChessPiece;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.ChessPieceAdapter;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.interfaces.GameDAO;
import model.GameData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.lang.Math.abs;

public class GameDAOMySQL extends DAOMySQL implements GameDAO
{
	private final String tableName = "gameData";

	public void makeMove(int gameID, ChessGame game) throws DataAccessException
	{
		String sql = "UPDATE gameData SET game = ? WHERE gameID = ?";

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				statement.setString(1, new Gson().toJson(game));
				statement.setInt(2, gameID);

				statement.executeUpdate();
			}
			catch(SQLException e)
			{
				if((e.getMessage().contains("gameID") && e.getMessage().contains("null"))
						|| e.getMessage().contains("empty"))
				{
					throw new DataAccessException("You must provide a gameID.");
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
	public GameData getGame(int gameID) throws DataAccessException
	{
		String sql = "SELECT * FROM " + tableName + " WHERE gameID = ?";

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				statement.setInt(1, gameID);

				try(var rs = statement.executeQuery())
				{
					if(rs.next())
					{
						String json = rs.getString("game");
						Gson gson = new GsonBuilder().registerTypeAdapter(ChessPiece.class,
								new ChessPieceAdapter()).create();
						ChessGame game = gson.fromJson(json, ChessGame.class);

						return new GameData(
								rs.getInt("gameID"),
								rs.getString("whiteUsername"),
								rs.getString("blackUsername"),
								rs.getString("gameName"),
								game
						);
					}
					else
					{
						throw new DataAccessException("A game with ID " + gameID + " does not exist.");
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
	public List<GameData> listGames()
	{
		String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM gameData";
		ArrayList<GameData> games = new ArrayList<>();

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				ResultSet rs = statement.executeQuery();

				while(rs.next())
				{
					String json = rs.getString("game");
					Gson gson = new GsonBuilder().registerTypeAdapter(ChessPiece.class,
							new ChessPieceAdapter()).create();
					ChessGame game = gson.fromJson(json, ChessGame.class);

					GameData gameData = new GameData
					(
						rs.getInt("gameID"),
						rs.getString("whiteUsername"),
						rs.getString("blackUsername"),
						rs.getString("gameName"),
						game
					);
					games.add(gameData);
				}
			}
		}
		catch(SQLException | DataAccessException e)
		{
			throw new RuntimeException(e);
		}

		return games;
	}

	@Override
	public boolean duplicateGame(String gameName)
	{
		String sql = "SELECT gameName FROM gameData";

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				ResultSet rs = statement.executeQuery();

				while(rs.next())
				{
					if(Objects.equals(rs.getString("gameName"), gameName))
					{
						return true;
					}
				}
			}
		}
		catch(SQLException | DataAccessException e)
		{
			throw new RuntimeException(e);
		}

		return false;
	}

	@Override
	public int newGame(String gameName) throws DataAccessException
	{
		String sql = "INSERT INTO " + tableName + " (gameID, whiteUsername, blackUsername, gameName, game)" +
				" VALUES(?, ?, ?, ?, ?)";

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				int gameID = abs(UUID.randomUUID().hashCode());

				ChessGame game = new ChessGame();
				Gson gson = new Gson();
				String jsonGame = gson.toJson(game);

				statement.setInt(1, gameID);
				statement.setString(2, null);
				statement.setString(3, null);
				statement.setString(4, gameName);
				statement.setString(5, jsonGame);

				statement.executeUpdate();

				return gameID;
			}
			catch(SQLException e)
			{
				if((e.getMessage().contains("gameName") && e.getMessage().contains("null"))
						|| e.getMessage().contains("empty"))
				{
					throw new DataAccessException("You must provide a game name.");
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
	public void joinGame(int gameID, ChessGame.TeamColor color, String username) throws DataAccessException
	{
		String sql;

		if(color == ChessGame.TeamColor.WHITE)
		{
			sql = "UPDATE " + tableName + " SET whiteUsername = ? WHERE gameID = ?";
		}
		else
		{
			sql = "UPDATE " + tableName + " SET blackUsername = ? WHERE gameID = ?";
		}

		try(Connection conn = DatabaseManager.getConnection())
		{
			try(var statement = conn.prepareStatement(sql))
			{
				statement.setString(1, username);
				statement.setInt(2, gameID);

				int rowsAffected = statement.executeUpdate();

				if(rowsAffected == 0)
				{
					throw new DataAccessException("Game with ID " + gameID + " does not exist.");
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

	@Override
	public boolean isEmpty()
	{
		return super.isEmpty(tableName);
	}
}

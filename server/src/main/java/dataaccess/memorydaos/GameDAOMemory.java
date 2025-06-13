package dataaccess.memorydaos;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.interfaces.GameDAO;
import model.GameData;

import java.util.*;

import static java.lang.Math.abs;

public class GameDAOMemory implements GameDAO
{
	private Map<Integer, GameData> idMap = new HashMap<>();

	@Override
	public void makeMove(int gameID, ChessGame chessGame) throws DataAccessException
	{
		GameData oldGameData = getGame(gameID);
		idMap.replace(gameID, new GameData(gameID, oldGameData.whiteUsername(),
				oldGameData.blackUsername(), oldGameData.gameName(), chessGame));
	}

	public GameData getGame(int gameID) throws DataAccessException
	{
		if(!idMap.containsKey(gameID))
		{
			throw new DataAccessException("A game with ID " + gameID + " does not exist.");
		}
		return idMap.get(gameID);
	}

	public List<GameData> listGames()
	{
        return new ArrayList<>(idMap.values());
	}

	public boolean duplicateGame(String gameName)
	{
		final boolean[] flag = {false};

		idMap.forEach((gameID, gameData) ->
		{
			if(gameData.gameName().equals(gameName))
			{
				flag[0] = true;
			}
		});

		return flag[0];
	}

	public int newGame(String gameName) throws DataAccessException
	{
		int gameID = abs(UUID.randomUUID().hashCode());

		if(gameName == null || gameName.isEmpty())
		{
			throw new DataAccessException("You must provide a game name.");
		}

		idMap.put(gameID, new GameData(gameID, null, null, gameName, new ChessGame()));
		return gameID;
	}

	public void joinGame(int gameID, ChessGame.TeamColor color, String username)
	{
		GameData game = idMap.get(gameID);
		GameData newGame;

		if(color == ChessGame.TeamColor.WHITE)
		{
			newGame = new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game());
		}
		else
		{
			newGame = new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game());
		}

		idMap.remove(gameID);
		idMap.put(gameID, newGame);
	}

	public void clear()
	{
		idMap.clear();
	}

	public boolean isEmpty()
	{
		return idMap.isEmpty();
	}
}

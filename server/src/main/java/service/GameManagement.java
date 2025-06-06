package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import model.GameData;
import model.Records;
import model.Records.JoinGameRequest;
import model.Records.NewGameRequest;
import model.Records.NewGameResult;

import java.util.List;

public class GameManagement
{
	private final AuthDAO authDAO;
	private final GameDAO gameDAO;

	public GameManagement(DAOManagement daoManager)
	{
		this.authDAO = daoManager.getAuthorizations();
		this.gameDAO = daoManager.getGames();
	}

	public NewGameResult makeGame(NewGameRequest request) throws DataAccessException
	{
		authDAO.authorizeToken(request.authToken());

		String gameName = request.gameName();
		if(!gameDAO.duplicateGame(gameName))
		{
			return new NewGameResult(gameDAO.newGame(gameName));
		}
		throw new DataAccessException("Game " + gameName + "already exists.");
	}

	public void joinGame(JoinGameRequest request) throws DataAccessException
	{
		String username = authDAO.authorizeToken(request.authToken());

		GameData game = gameDAO.getGame(request.gameID());

		gameDAO.joinGame(game.gameID(), teamJoin(game, request.playerColor()), username);
	}

	private ChessGame.TeamColor teamJoin(GameData game, ChessGame.TeamColor color) throws DataAccessException
	{
		if(color == ChessGame.TeamColor.BLACK && game.blackUsername() == null)
		{
			return ChessGame.TeamColor.BLACK;
		}
		else if(color == ChessGame.TeamColor.WHITE && game.whiteUsername() == null)
		{
			return ChessGame.TeamColor.WHITE;
		}
		else if(color != ChessGame.TeamColor.WHITE && color != ChessGame.TeamColor.BLACK)
		{
			throw new DataAccessException("Invalid team color.");
		}
		throw new DataAccessException("Another user has already claimed the " + color + " team in this game.");
	}

	public List<Records.ListedGame> listGames(String authToken) throws DataAccessException
	{
		authDAO.authorizeToken(authToken);

		return gameDAO.listGames();
	}
}
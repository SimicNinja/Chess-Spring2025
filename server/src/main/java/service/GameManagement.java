package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import model.GameData;
import model.Records.JoinGameRequest;
import model.Records.ListedGame;
import model.Records.NewGameRequest;
import model.Records.NewGameResult;

import java.util.List;

public class GameManagement
{
	private final AuthDAO authorizations;
	private final GameDAO games;

	public GameManagement(DAOManagement daoManager)
	{
		this.authorizations = daoManager.getAuthorizations();
		this.games = daoManager.getGames();
	}

	public NewGameResult makeGame(NewGameRequest request) throws DataAccessException
	{
		authorizations.authorizeToken(request.authToken());

		String gameName = request.gameName();
		if(!games.duplicateGame(gameName))
		{
			return new NewGameResult(games.newGame(gameName));
		}
		throw new DataAccessException("Game " + gameName + "already exists.");
	}

	public void joinGame(JoinGameRequest request) throws DataAccessException
	{
		String username = authorizations.authorizeToken(request.authToken());

		GameData game = games.getGame(request.gameID());

		games.joinGame(game.gameID(), teamJoin(game, request.playerColor()), username);
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

	public List<ListedGame> listGames(String authToken) throws DataAccessException
	{
		authorizations.authorizeToken(authToken);

		return games.listGames();
	}
}
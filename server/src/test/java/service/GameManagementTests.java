package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.GameData;
import model.Records.*;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagementTests
{
	private static DAOManagement daoManager;
	private static UserManagement userManager;
	private static GameManagement gameManager;
	String authToken;
	int gameID;

	@BeforeAll
	public static void init()
	{
		daoManager = new DAOManagement();
		userManager = new UserManagement(daoManager);
		gameManager = new GameManagement(daoManager);
	}

	@BeforeEach
	public void setup()
	{
		try
		{
			daoManager.clearApplication();

			LoginResult result = userManager.register(new UserData("LickyFrog", "greenTreeFrog", "amazon@gmail.com"));
			authToken = result.authToken();

			NewGameResult result2 = gameManager.makeGame(new NewGameRequest(authToken, "TestGame"));
			gameID = result2.gameID();
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Make a Game")
	public void makeGameTest()
	{
		daoManager.clearApplication();

		try
		{
			daoManager.clearApplication();

			LoginResult result = userManager.register(new UserData("LickyFrog", "greenTreeFrog", "amazon@gmail.com"));
			String authToken = result.authToken();

			NewGameResult actual = gameManager.makeGame(new NewGameRequest(authToken, "TestGame"));

			assertNotNull(actual, "Result should not be null.");
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Missing Game Name")
	public void missingName()
	{
		Exception e = assertThrows(DataAccessException.class, () ->
				gameManager.makeGame(new NewGameRequest(authToken, "")));

		assertTrue(e.getMessage().contains("You must provide a game name"));
	}

	@Test
	@DisplayName("Duplicate Game")
	public void duplicateTest()
	{
		Exception e = assertThrows(DataAccessException.class, () ->
				gameManager.makeGame(new NewGameRequest(authToken, "TestGame")));

		assertTrue(e.getMessage().contains("already exists"));
	}

	@Test
	@DisplayName("Invalid User Creates Game")
	public void unauthorizedToMakeGameTest()
	{
		Exception e = assertThrows(DataAccessException.class, () ->
				gameManager.makeGame(new NewGameRequest("FakeToken", "TestGame")));

		assertTrue(e.getMessage().contains("no authorization"));
	}

	@Test
	@DisplayName("LickyFrog Joins White")
	public void joinWhiteTest()
	{
		try
		{
			gameManager.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, gameID));

			assertEquals("LickyFrog", daoManager.getGames().getGame(gameID).whiteUsername());
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("LickyFrog Joins Black")
	public void joinBlackTest()
	{
		try
		{
			gameManager.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.BLACK, gameID));

			assertEquals("LickyFrog", daoManager.getGames().getGame(gameID).blackUsername());
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Invalid GameID to Join")
	public void badGameIDTest()
	{
		Exception e = assertThrows(DataAccessException.class, () ->
				gameManager.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, -1)));

		assertTrue(e.getMessage().contains("A game with"));
	}

	@Test
	@DisplayName("Invalid User Joins Game")
	public void unauthorizedToJoinGameTest()
	{
		Exception e = assertThrows(DataAccessException.class, () ->
				gameManager.joinGame(new JoinGameRequest("FakeToken", ChessGame.TeamColor.WHITE, gameID)));

		assertTrue(e.getMessage().contains("no authorization"));
	}

	@Test
	@DisplayName("Try to Join Occupied Team")
	public void occupiedTeamTest()
	{
		try
		{
			LoginResult result = userManager.register(new UserData("SecondFrog", "1234", "nu@gmail.com"));
			String secondAuthToken = result.authToken();

			gameManager.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, gameID));

			Exception e = assertThrows(DataAccessException.class, () ->
					gameManager.joinGame(new JoinGameRequest(secondAuthToken, ChessGame.TeamColor.WHITE, gameID)));

			assertTrue(e.getMessage().contains("Another user has already claimed"));
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Successfully List Games")
	public void listGamesTest()
	{
		try
		{
			//Setup Conditions
			NewGameResult result2 = gameManager.makeGame(new NewGameRequest(authToken, "TestGame2"));
			int gameID2 = result2.gameID();

			//Setup Expected
			GameData game1 = new GameData(gameID, null, null, "TestGame", new ChessGame());
			GameData game2 = new GameData(gameID2, null, null, "TestGame2", new ChessGame());

			//Calculate Actual
			List<GameData> gameList = gameManager.listGames(authToken);

			assertEquals(new HashSet<>(List.of(game1, game2)), new HashSet<>(gameList));
		}
		catch(DataAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}
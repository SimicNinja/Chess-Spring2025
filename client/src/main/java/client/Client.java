package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import serverfacade.ResponseException;
import serverfacade.ServerFacade;
import websocket.GameClient;

import java.util.Arrays;
import java.util.List;

import static ui.EscapeSequences.*;

public class Client
{
	private String username = null;
	private String authToken = "";
	private static ServerFacade facade;
	private boolean signedIn = false;
	private List<GameData> games;
	private final REPL repl;

	public Client(String serverUrl, REPL repl)
	{
		facade = new ServerFacade(serverUrl);
        this.repl = repl;
    }

	public String eval(String input) throws ResponseException
	{
		var tokens = input.split(" ");
		var cmd = (tokens.length > 0) ? tokens[0] : "help";
		var params = Arrays.copyOfRange(tokens, 1, tokens.length);
		return switch(cmd)
        {
            case "register" -> register(params);
            case "login" -> login(params);
			case "logout" -> logout(params);
			case "create" -> createGame(params);
			case "list" -> list(params);
			case "join" -> joinGame(params);
			case "observe" -> observeGame(params);
            case "quit" -> "quit";
			case "help" -> help();
            default ->
            {
                System.out.print(SET_TEXT_COLOR_RED + "Error: Not a valid command.\n");
                yield help();
            }
        };
	}

	public String register(String... params) throws ResponseException
	{
		assertCommandLength(3, "Expected <username> <password> <email>", params);

		username = params[0];
		AuthData auth = facade.register(username, params[1], params[2]);
		authToken = auth.authToken();

		signedIn = true;
		games = facade.listGames(authToken);

		return String.format("You have made a new account!" +
				"\nUsername: " + username +
				"\nEmail: " + params[2]);
	}

	public String login(String... params) throws ResponseException
	{
		assertCommandLength(2, "Expected: <username> <password>", params);

		username = params[0];
		AuthData auth = facade.login(username, params[1]);
		authToken = auth.authToken();

		signedIn = true;
		games = facade.listGames(authToken);

		return String.format("You signed in as %s.\n" + help(), username);
	}

	public String logout(String... params) throws ResponseException
	{
		assertSignedIn();
		assertCommandLength(0, "Logout command has no additional inputs", params);

		signedIn = false;
		facade.logout(authToken);

		return "You have successfully logged out!";
	}

	public String list(String... params) throws ResponseException
	{
		assertSignedIn();
		assertCommandLength(0, "List command has no additional inputs", params);

		StringBuilder output = new StringBuilder();

		games = facade.listGames(authToken);

		output.append(SET_TEXT_UNDERLINE + "Games\n" + RESET_TEXT_UNDERLINE);

		if(games.isEmpty())
		{
			output.append(SET_TEXT_COLOR_YELLOW + "No games have been made. Use the create command to make one.");
		}
		else
		{
			for (int i = 0; i < games.size(); i++)
			{
				GameData game = games.get(i);
				output.append(SET_TEXT_COLOR_BLUE + (i + 1) + "-" + "Name:" + RESET_TEXT_COLOR + game.gameName());
				output.append(SET_TEXT_COLOR_BLUE + " White:" + RESET_TEXT_COLOR + listUser(game.whiteUsername()));
				output.append(SET_TEXT_COLOR_BLUE + " Black:" + RESET_TEXT_COLOR + listUser(game.blackUsername()) + "\n");
			}
		}

		return String.valueOf(output);
	}

	private String listUser(String username)
	{
		if(username == null)
		{
			return SET_TEXT_COLOR_YELLOW + "Empty use join command!";
		}
		return username;
	}

	public String createGame(String... params) throws ResponseException
	{
		assertSignedIn();
		assertCommandLength(1, "Expected: <name>", params);

		facade.newGame(authToken, params[0]);

		return "Successfully created game " + params[0];
	}

	public String joinGame(String... params) throws ResponseException
	{
		assertSignedIn();
		assertCommandLength(2, "Expected: <ID> [White/Black]\n" + SET_TEXT_COLOR_YELLOW +
				"Please use the id used from the list command.", params);

		int clientGameID = validateGameID(params[0]);
		ChessGame.TeamColor color = validateTeamColor(params[1]);
		GameData game = games.get(clientGameID - 1);

		facade.joinGame(authToken, color, game.gameID());

		//Update private class members from server
		games = facade.listGames(authToken);
		game = games.get(clientGameID - 1);

		GameClient gameClient = new GameClient(game, color, authToken);
		repl.passGameClient(gameClient);

		return gameClient.joinGame(authToken, username, color);
	}

	public String observeGame(String... params) throws ResponseException
	{
		assertSignedIn();
		assertCommandLength(1, "Expected: <ID> \n" + SET_TEXT_COLOR_YELLOW +
				"Please use the id used from the list command.", params);

		int clientGameID = validateGameID(params[0]);
		GameData game = games.get(clientGameID - 1);

		GameClient gameClient = new GameClient(game, ChessGame.TeamColor.WHITE, authToken);
		repl.passGameClient(gameClient);

		return gameClient.observeGame(authToken);
	}

	private ChessGame.TeamColor validateTeamColor(String input) throws ResponseException
	{
		if(input.equals("White") || input.equals("white") || input.equals("WHITE"))
		{
			return ChessGame.TeamColor.WHITE;
		}
		else if(input.equals("Black") || input.equals("black") || input.equals("BLACK"))
		{
			return ChessGame.TeamColor.BLACK;
		}
		throw new ResponseException(400, "Bad team color" + SET_TEXT_COLOR_YELLOW + "\nTeam color must be white or black!");
	}

	private int validateGameID(String input) throws ResponseException
	{
		int gameID;

		try
		{
			gameID = Integer.parseInt(input);
		}
		catch(Exception e)
		{
			throw  new ResponseException(400, "Bad game id" + SET_TEXT_COLOR_YELLOW + "\nGame IDs can only be numbers.");
		}

		if(gameID < 0 || gameID > games.size())
		{
			throw new ResponseException(400, "Bad game id" + SET_TEXT_COLOR_YELLOW + "\nPlease use the id # from the list command.");
		}
		return gameID;
	}

	public String help()
	{
		if(!signedIn)
		{
			return SET_TEXT_COLOR_WHITE + """ 
				- login <username> <password>
				- register <username> <password> <email>
				- quit
				- help
				""";
		}
		return SET_TEXT_COLOR_WHITE + """
			- create <name> - Creates a new game with the given name.
			- list - Lists all games.
			- join <ID> [White or Black] - Adds you to the specified team color and game.
			- observe <ID> - Allows you to spectate the specified game.
			- logout
			- quit
			- help
			""";
	}

	public boolean isSignedIn()
	{
		return signedIn;
	}

	private void assertSignedIn() throws ResponseException
	{
		if(!signedIn)
		{
			throw new ResponseException(400, "You must sign in");
		}
	}

	private void assertCommandLength(int length, String message, String... params) throws ResponseException
	{
		if(params.length != length)
		{
			throw new ResponseException(400, message);
		}
	}
}

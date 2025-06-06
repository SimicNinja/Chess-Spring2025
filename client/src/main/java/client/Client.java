package client;

import chess.ChessGame;
import model.AuthData;
import model.Records;
import serverfacade.ResponseException;
import serverfacade.ServerFacade;

import java.util.Arrays;
import java.util.List;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_WHITE;

public class Client
{
	private String username = null;
	private String authToken = "";
	private static ServerFacade facade;
	private boolean signedIn = false;
	private List<Records.ListedGame> games;

	public Client(String serverUrl)
	{
		facade = new ServerFacade(serverUrl);
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
			case "create" -> createGame(params);
			case "list" -> list(params);
			case "join" -> joinGame(params);
            case "quit" -> "quit";
            default ->
            {
                System.out.print(SET_TEXT_COLOR_RED + "Error: Not a valid command.\n");
                yield help();
            }
        };
	}

	public String register(String... params) throws ResponseException
	{
		if(params.length == 3)
		{
			username = params[0];
			AuthData auth = facade.register(username, params[1], params[2]);
			authToken = auth.authToken();

			signedIn = true;
			games = facade.listGames(authToken);

			return String.format("You have made a new account!" +
					"\nUsername: " + username +
					"\nPassword: " + params[1] +
					"\nEmail: " + params[2]);
		}
		throw new ResponseException(400, "Expected: <username> <password> <email>");
	}

	public String login(String... params) throws ResponseException
	{
		if(params.length == 2)
		{
			username = params[0];
			AuthData auth = facade.login(username, params[1]);
			authToken = auth.authToken();

			signedIn = true;
			games = facade.listGames(authToken);

			return String.format("You signed in as %s.\n" + help(), username);
		}
		throw new ResponseException(400, "Expected: <username> <password>");
	}

	public String list(String... params) throws ResponseException
	{
		assertSignedIn();

		StringBuilder output = new StringBuilder();

		if(params.length == 0)
		{
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
					Records.ListedGame game = games.get(i);
					output.append(SET_TEXT_COLOR_BLUE + (i + 1) + "-" + "Name:" + RESET_TEXT_COLOR + game.gameName());
					output.append(SET_TEXT_COLOR_BLUE + " White:" + RESET_TEXT_COLOR + listUser(game.whiteUsername()));
					output.append(SET_TEXT_COLOR_BLUE + " Black:" + RESET_TEXT_COLOR + listUser(game.blackUsername()) + "\n");
				}
			}

			return String.valueOf(output);
		}
		throw new ResponseException(400, "List command has no additional inputs");
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

		if(params.length == 1)
		{
			facade.newGame(authToken, params[0]);

			return "Successfully created game " + params[0];
		}
		throw new ResponseException(400, "Expected: <name>");
	}

	public String joinGame(String... params) throws ResponseException
	{
		assertSignedIn();

		if(params.length == 2)
		{
			int idInput;
			String colorInput = params[1];
			ChessGame.TeamColor color;
			int gameID;
			Records.ListedGame game;

			try
			{
				idInput = Integer.parseInt(params[0]);
			}
			catch(Exception e)
			{
				throw  new ResponseException(400, "Bad game id" + SET_TEXT_COLOR_YELLOW + "\nGame IDs can only be numbers.");
			}

			if(colorInput.equals("White") || colorInput.equals("white") || colorInput.equals("WHITE"))
			{
				color = ChessGame.TeamColor.WHITE;
			}
			else if(colorInput.equals("Black") || colorInput.equals("black") || colorInput.equals("BLACK"))
			{
				color = ChessGame.TeamColor.BLACK;
			}
			else
			{
				throw new ResponseException(400, "Bad team color" + SET_TEXT_COLOR_YELLOW + "\nTeam color must be white or black!");
			}

			if(idInput < 0 || idInput > games.size())
			{
				throw new ResponseException(400, "Bad game id" + SET_TEXT_COLOR_YELLOW + "\nPlease use the id # from the list command.");
			}
			else
			{
				game = games.get(idInput - 1);
				gameID = game.gameID();
			}

			facade.joinGame(authToken, color, gameID);

			return "You have joined game #" + idInput + " " + RESET_TEXT_COLOR + game.gameName() + SET_TEXT_COLOR_BLUE
					+ " on the " + color + " team as " + RESET_TEXT_COLOR + username + SET_TEXT_COLOR_BLUE +".";
		}
		throw new ResponseException(400, "Expected: <ID> [White/Black]\n" + SET_TEXT_COLOR_YELLOW +
				"Please use the id used from the list command.");
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

	private void assertSignedIn() throws ResponseException
	{
		if(!signedIn)
		{
			throw new ResponseException(400, "You must sign in");
		}
	}

	public boolean isSignedIn()
	{
		return signedIn;
	}
}

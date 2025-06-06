package client;

import model.AuthData;
import serverfacade.ResponseException;
import serverfacade.ServerFacade;

import java.util.Arrays;

public class Client
{
	private String username = null;
	private String authToken = "";
	private static ServerFacade facade;
	private boolean signedIn = false;

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
			case "quit" -> "quit";
			default -> help();
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
			return String.format("You have made a new account!" +
					"\nUsername: " + username +
					"\nPassword: " + params[1] +
					"\nEmail: " + params[2]);
		}
		throw new ResponseException(400, "Expected: <username> <password> <email>");
	}

	public String login(String... params) throws ResponseException
	{
		if(params.length >= 1)
		{
			signedIn = true;
			username = String.join("-", params);
			return String.format("You signed in as %s.", username);
		}
		throw new ResponseException(400, "Expected: <username>");
	}

	public String help()
	{
		if(!signedIn)
		{
			return """
				- login <username> <password>
				- register <username> <password> <email>
				- quit
				- help
				""";
		}
		return """
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
}

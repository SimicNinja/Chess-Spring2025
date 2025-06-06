import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.Records.*;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerFacade
{

	private final String serverUrl;

	public ServerFacade(String url)
	{
		serverUrl = url;
	}

	public void clear() throws ResponseException
	{
		this.makeRequest("DELETE", "/db", null, null, null);
	}

	public AuthData register(String username, String password, String email) throws ResponseException
	{
		UserData newUser = new UserData(username, password, email);
		return this.makeRequest("POST", "/user", newUser, null, AuthData.class);
	}

	public AuthData login(String username, String password) throws ResponseException
	{
		LoginRequest loginInfo = new LoginRequest(username, password);
		return this.makeRequest("POST", "/session", loginInfo, null, AuthData.class);
	}

	public void logout(String authToken) throws ResponseException
	{
		this.makeRequest("DELETE", "/session", null, makeAuth(authToken), null);
	}

	public NewGameResult newGame(String authToken, String gameName) throws ResponseException
	{
		NewGameRequest req = new NewGameRequest(authToken, gameName);
		return this.makeRequest("POST", "/game", req, makeAuth(authToken), NewGameResult.class);
	}

	public void joinGame(String authToken, ChessGame.TeamColor color, int gameID) throws ResponseException
	{
		JoinGameRequest req = new JoinGameRequest(authToken, color, gameID);
		this.makeRequest("PUT", "/game", req, makeAuth(authToken), null);
	}

	public List<ListedGame> listGames(String authToken) throws ResponseException
	{
		GamesList gameList = this.makeRequest("GET", "/game", null, makeAuth(authToken), GamesList.class);
		return gameList.getGames();
	}

	private <T> T makeRequest(String method, String path, Object request,
							  Map<String, String> headers, Class<T> responseClass)
			throws ResponseException
	{
		try
		{
			URL url = (new URI(serverUrl + path)).toURL();
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod(method);
			http.setDoOutput(true);

			if(headers != null)
			{
				for(Map.Entry<String, String> header : headers.entrySet())
				{
					http.setRequestProperty(header.getKey(), header.getValue());
				}
			}


			writeBody(request, http);
			http.connect();
			throwIfNotSuccessful(http);
			return readBody(http, responseClass);
		}
		catch(ResponseException ex)
		{
			throw ex;
		}
		catch(Exception ex)
		{
			throw new ResponseException(500, ex.getMessage());
		}
	}


	private static void writeBody(Object request, HttpURLConnection http) throws IOException
	{
		if(request != null)
		{
			http.addRequestProperty("Content-Type", "application/json");
			String reqData = new Gson().toJson(request);
			try(OutputStream reqBody = http.getOutputStream())
			{
				reqBody.write(reqData.getBytes());
			}
		}
	}

	private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException
	{
		var status = http.getResponseCode();
		String message = "Unknown Error; Please Try Again";
		if(!isSuccessful(status))
		{
			message = switch(status)
			{
				case 400 -> "Error: Bad Request";
				case 401 -> "Error: Unauthorized";
				case 403 -> "Error: Already Taken";
				default -> message;
			};
			throw new ResponseException(status, message);
		}
	}

	private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException
	{
		T response = null;
		if(http.getContentLength() < 0)
		{
			try(InputStream respBody = http.getInputStream())
			{
				InputStreamReader reader = new InputStreamReader(respBody);
				if(responseClass != null)
				{
					response = new Gson().fromJson(reader, responseClass);
				}
			}
		}
		return response;
	}

	private Map<String, String> makeAuth(String authToken)
	{
		Map<String, String> header = new HashMap<>();
		header.put("Authorization", authToken);
		return header;
	}

	private boolean isSuccessful(int status)
	{
		return status / 100 == 2;
	}

	public static class GamesList
	{
		private List<ListedGame> games;

		public GamesList() {}

		public List<ListedGame> getGames()
		{
			return games;
		}
	}
}